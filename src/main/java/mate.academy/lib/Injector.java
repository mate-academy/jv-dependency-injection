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
    private static final Map<Class<?>, Class<?>> IMPLEMENTATIONS = Map.of(
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class
    );
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public <T> T getInstance(Class<T> interfaceClazz) {
        Class<?> implClass = findImplementation(interfaceClazz);

        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Injection failed, missing @Component for class: " + implClass.getName()
            );
        }

        if (instances.containsKey(implClass)) {
            return interfaceClazz.cast(instances.get(implClass));
        }

        Object instance = createInstance(implClass);
        instances.put(implClass, instance);
        injectFields(implClass, instance);

        return interfaceClazz.cast(instance);
    }

    private Object createInstance(Class<?> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of: " + clazz.getName(), e);
        }
    }

    private void injectFields(Class<?> clazz, Object instance) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object dependency = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(instance, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't inject field: " + field.getName(), e);
                }
            }
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            return interfaceClazz;
        }
        Class<?> impl = IMPLEMENTATIONS.get(interfaceClazz);
        if (impl == null) {
            throw new RuntimeException(
                    "No implementation found for: " + interfaceClazz.getName()
            );
        }
        return impl;
    }
}
