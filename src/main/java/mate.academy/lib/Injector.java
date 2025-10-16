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
    private final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        Object clazzImplementationInstance = createNewInstance(clazz);
        instances.put(clazz, clazzImplementationInstance);
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                            "Can't initialize field value. Class: "
                                    + clazz.getName() + ". Field: " + field.getName(), e);
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Implementation class " + clazz.getName()
                    + " is not annotated with @Component");
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            Class<?> implementation = interfaceImplementations.get(interfaceClazz);
            if (implementation == null) {
                throw new RuntimeException("No implementation found for "
                        + interfaceClazz.getName());
            }
            if (!implementation.isAnnotationPresent(Component.class)) {
                throw new RuntimeException("Implementation class "
                        + implementation.getName() + " is not annotated with @Component");
            }
            return implementation;
        }
        if (!interfaceImplementations.containsValue(interfaceClazz)) {
            throw new RuntimeException("Unsupported class passed: "
                    + interfaceClazz.getName());
        }
        return interfaceClazz;
    }
}
