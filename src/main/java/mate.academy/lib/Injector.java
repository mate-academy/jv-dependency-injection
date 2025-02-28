package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new ConcurrentHashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementation = findImplementation(interfaceClazz);
        if (!implementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Missing @Component on class: "
                    + implementation.getName());
        }
        return instances.computeIfAbsent(implementation, this::createInstance);
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            return interfaceClazz;
        }
        return switch (interfaceClazz.getSimpleName()) {
            case "FileReaderService" -> mate.academy.service.impl
                    .FileReaderServiceImpl.class;
            case "ProductParser" -> mate.academy.service.impl
                    .ProductParserImpl.class;
            case "ProductService" -> mate.academy.service.impl
                    .ProductServiceImpl.class;
            default -> throw new RuntimeException("No implementation found for: "
                    + interfaceClazz.getName());
        };
    }

    private Object createInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            Object instance = constructor.newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    Object dependency = getInstance(field.getType());
                    field.set(instance, dependency);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance for: "
                    + clazz.getName(), e);
        }
    }
}
