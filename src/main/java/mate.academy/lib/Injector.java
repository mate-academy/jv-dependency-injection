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

    private static final Map<Class<?>, Class<?>> IMPLEMENTATION_MAP = Map.of(
            ProductService.class, ProductServiceImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class
    );

    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz.getName()
                    + " is missing @Component annotation");
        }
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        Object clazzImplementationInstance = createNewInstance(clazz);
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class "
                            + clazz.getName() + ". Field: " + field.getName(), e);
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(final Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object object = constructor.newInstance();
            instances.put(clazz, object);
            return object;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            return interfaceClazz;
        }
        Class<?> implementation = IMPLEMENTATION_MAP.get(interfaceClazz);
        if (implementation == null) {
            throw new RuntimeException("No implementation found for interface: "
                    + interfaceClazz.getName());
        }
        return implementation;
    }
}
