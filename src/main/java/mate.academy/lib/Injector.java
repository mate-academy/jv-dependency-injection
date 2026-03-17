package mate.academy.lib;

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

    private final Map<Class<?>, Class<?>> interfaceImplementationMap = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementation = findImplementation(interfaceClazz);
        if (!implementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Class " + implementation.getName() + " is missing @Component annotation");
        }
        if (instances.containsKey(implementation)) {
            return instances.get(implementation);
        }
        try {
            Object instance = implementation.getDeclaredConstructor().newInstance();
            instances.put(implementation, instance);
            injectDependencies(instance, implementation);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create instance of "
                    + implementation.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> clazz) {
        if (clazz.isInterface()) {
            Class<?> impl = interfaceImplementationMap.get(clazz);
            if (impl == null) {
                throw new RuntimeException("No implementation found for interface "
                        + clazz.getName());
            }
            return impl;
        }
        return clazz;
    }

    private void injectDependencies(Object instance, Class<?> implClass)
            throws ReflectiveOperationException {
        for (Field field : implClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Class<?> dependencyClass = findImplementation(field.getType());
                Object dependency = getInstance(dependencyClass);
                field.setAccessible(true);
                field.set(instance, dependency);
            }
        }
    }
}
