package mate.academy.lib;

import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Injector {
    private static final Injector injector = new Injector();

    public Injector() {
        implementations.put(ProductParser.class, ProductParserImpl.class);
        implementations.put(ProductService.class, ProductServiceImpl.class);
        implementations.put(FileReaderService.class, FileReaderServiceImpl.class);
    }

    private final Set<Class<?>> creating = new HashSet<>();
    private final Map<Class<?>, Class<?>> implementations = new HashMap<>();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> inputClazz) {
        Class<?> clazz = resolveImplementation(inputClazz);
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't find the annotation!");
        }
        if (creating.contains(clazz)) {
            throw new RuntimeException("Cyclic dependency detected: " + clazz);
        }
        creating.add(clazz);
        Object instance;
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            instance = constructor.newInstance();
            instances.put(clazz, instance);
            for (Field field : clazz.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Inject.class)) continue;
                Class<?> fieldType = field.getType();
                Class<?> impl = resolveImplementation(fieldType);
                Object dependency = getInstance(impl);
                field.setAccessible(true);
                field.set(instance, dependency);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("You can't instantiated: " + inputClazz);
        } finally {
            creating.remove(clazz);
        }
        return instance;
    }

    private Class<?> resolveImplementation(Class<?> fieldType) {
        if (!fieldType.isInterface()) {
            return fieldType;
        }
        Class<?> impl = implementations.get(fieldType);
        if (impl == null) {
            throw new RuntimeException("No implementations found for " + fieldType);
        }
        return impl;
    }
}

