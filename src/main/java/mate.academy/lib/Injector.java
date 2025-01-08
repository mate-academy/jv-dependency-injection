package mate.academy.lib;

import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            throw new RuntimeException("interfaceClazz should be an interface but got: " + interfaceClazz);
        }

        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }

        Object implementation = initializeImplementation(interfaceClazz);
        instances.put(interfaceClazz, implementation);
        return implementation;
    }

    private Object initializeImplementation(Class<?> interfaceClazz) {
        Class<?> implClazz = findImplementation(interfaceClazz);
        if (implClazz == null) {
            throw new RuntimeException("No implementation found for: " + interfaceClazz);
        }
        return createInstance(implClazz);
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        for (Class<?> clazz : getAllClassesInPackage()) {
            if (interfaceClazz.isAssignableFrom(clazz) && clazz.isAnnotationPresent(Component.class)) {
                return clazz;
            }
        }
        return null;
    }

    private Object createInstance(Class<?> clazz) {
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            injectDependencies(instance);
            return instance;
        } catch (InstantiationException | IllegalAccessException |
                 InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to create instance of: " + clazz, e);
        }
    }

    private void injectDependencies(Object instance) {
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object dependency = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(instance, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject dependency into: " + field.getName(), e);
                }
            }
        }
    }

    private Set<Class<?>> getAllClassesInPackage() {
        return Set.of(FileReaderServiceImpl.class, ProductParserImpl.class, ProductServiceImpl.class);
    }

}
