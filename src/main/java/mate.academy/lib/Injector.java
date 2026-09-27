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
    private static final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object instance = null;
        Class<?> clazz = getImplementation(interfaceClazz);

        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());

                instance = createNewInstance(clazz);

                field.setAccessible(true);
                try {
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                            "Can't initialize field value. " + field.getName(), e
                    );
                }
            }
        }

        if (instance == null) {
            instance = createNewInstance(clazz);
        }

        return instance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Object instance = clazz.getConstructor().newInstance();
            instances.put(clazz, instance);
            return instance;

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create an instance of " + clazz.getName(), e);
        }
    }

    private Class<?> getImplementation(Class<?> interfaceClazz) {
        Class<?> implementation = IMPLEMENTATIONS.get(interfaceClazz);

        if (implementation == null) {
            throw new RuntimeException(
                    "No implementation found for " + interfaceClazz.getName()
            );
        }

        if (!implementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "The class is not marked as @Component: " + implementation.getName()
            );
        }

        return implementation;
    }
}
