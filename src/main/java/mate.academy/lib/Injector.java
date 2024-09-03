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
        if (!interfaceClazz.isInterface()) {
            throw new RuntimeException("Only interfaces are supported");
        }

        Class<?> implClass = findImplementation(interfaceClazz);
        if (implClass == null || !implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("No implementation found for " + interfaceClazz.getName());
        }

        return createInstance(implClass);
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        // This method should return the implementation class for the given interface
        // For simplicity, let's assume we have a hardcoded map of implementations
        Map<Class<?>, Class<?>> implementations = Map.of(
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class
        );
        return implementations.get(interfaceClazz);
    }

    private Object createInstance(Class<?> implClass) {
        if (instances.containsKey(implClass)) {
            return instances.get(implClass);
        }

        try {
            Object instance = implClass.getDeclaredConstructor().newInstance();
            instances.put(implClass, instance);

            for (Field field : implClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    Object fieldInstance = getInstance(field.getType());
                    field.set(instance, fieldInstance);
                }
            }

            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create instance of " + implClass.getName(), e);
        }
    }
}
