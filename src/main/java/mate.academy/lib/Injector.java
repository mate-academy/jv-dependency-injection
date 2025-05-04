package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class
    );

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzImplementation = createNewInstance(clazz);
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementation, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Сan't initialize field value. "
                            + "Class: " + clazz.getName() + ". Field: "
                            + field.getName());
                }
            }
        }
        if (clazzImplementation == null) {
            clazzImplementation = createNewInstance(clazz);
        }
        return clazzImplementation;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            Class<?> clazz = interfaceImplementations.get(interfaceClazz);
            validateComponent(clazz);
            return clazz;
        }

        validateComponent(interfaceClazz);
        return interfaceClazz;
    }

    private void validateComponent(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz.getName()
                    + " is not marked with @Component");
        }
    }
}
