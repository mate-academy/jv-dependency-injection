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
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);

        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new IllegalStateException("Class " + clazz.getName()
                    + " is not annotated with @Component");
        }

        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        Object instance = createNewInstance(clazz);
        instances.put(clazz, instance);
        injectDependencies(instance);

        return instance;
    }

    private void injectDependencies(Object instance) {
        Class<?> clazz = instance.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field " + field.getName()
                            + " in " + clazz.getName(), e);
                }
            }
        }
    }

    private Object createNewInstance(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);

        if (interfaceClazz.isInterface()) {
            Class<?> impl = interfaceImplementations.get(interfaceClazz);
            System.out.println("Implementation for " + interfaceClazz.getName() + ": "
                    + (impl != null ? impl.getName() : "null"));

            if (impl == null) {
                throw new RuntimeException("No implementation found for "
                        + interfaceClazz.getName());
            }
            return impl;
        }
        return interfaceClazz;
    }
}
