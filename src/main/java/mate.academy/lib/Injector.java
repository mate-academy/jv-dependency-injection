package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();

    private Injector() {
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        return getInstance(interfaceClazz, new HashSet<>());
    }

    private Object getInstance(Class<?> interfaceClazz, Set<Class<?>> recursionGuard) {
        Class<?> clazz = findImplementation(interfaceClazz);

        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new IllegalStateException("Class " + clazz.getName()
                    + " is not annotated with @Component");
        }

        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        if (!recursionGuard.add(clazz)) {
            throw new RuntimeException("Cyclic dependency detected for "
                    + clazz.getName());
        }

        Object instance = createNewInstance(clazz);
        instances.put(clazz, instance);
        injectDependencies(instance, recursionGuard);

        return instance;
    }

    private void injectDependencies(Object instance, Set<Class<?>> recursionGuard) {
        Class<?> clazz = instance.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType(), recursionGuard);
                try {
                    field.setAccessible(true);
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field "
                            + field.getName()
                            + " in " + clazz.getName(), e);
                }
            }
        }
    }

    private Object createNewInstance(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of "
                    + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            Class<?> impl = interfaceImplementations.get(interfaceClazz);
            if (impl == null) {
                throw new RuntimeException("No implementation found for "
                        + interfaceClazz.getName());
            }
            return impl;
        }
        return interfaceClazz;
    }
}
