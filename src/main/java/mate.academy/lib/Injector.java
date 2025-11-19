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
    private static final Map<Class<?>, Object> instances = new HashMap<>();
    private static final Map<Class<?>, Class<?>> interfaceImplementations =
            Map.of(FileReaderService.class, FileReaderServiceImpl.class,
                    ProductParser.class, ProductParserImpl.class,
                    ProductService.class, ProductServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class \"" + clazz + "\" is not annotated with @Component.");
        }
        Object clazzInstance = null;
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzInstance = createNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Unable to initialize field value. Class: "
                            + clazz.getName() + ", Field: " + field.getName(), e);
                }
            }
        }
        if (clazzInstance == null) {
            clazzInstance = createNewInstance(clazz);
        }
        return clazzInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        Constructor<?> constructor = null;
        try {
            constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Unable to create an instance of: " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClass) {
        if (interfaceClass.isInterface()) {
            return interfaceImplementations.get(interfaceClass);
        }
        return interfaceClass;
    }
}
