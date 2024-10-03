package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        // Try to resolve implementation for the given interface
        Class<?> implClass = findImplementation(interfaceClazz);

        // Check if class is annotated with @Component
        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + implClass.getName()
                    + " is not marked as @Component");
        }

        // Check if an instance already exists
        if (instances.containsKey(implClass)) {
            return instances.get(implClass);
        }

        // Create a new instance and inject dependencies
        Object instance = createInstance(implClass);
        instances.put(implClass, instance);
        return instance;
    }

    private Object createInstance(Class<?> implClass) {
        try {
            Object instance = implClass.getDeclaredConstructor().newInstance();

            // Inject dependencies into fields annotated with @Inject
            for (Field field : implClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    Object fieldInstance = getInstance(field.getType());
                    field.set(instance, fieldInstance);
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + implClass.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            if (interfaceClazz == mate.academy.service.FileReaderService.class) {
                return mate.academy.service.impl.FileReaderServiceImpl.class;
            } else if (interfaceClazz == mate.academy.service.ProductParser.class) {
                return mate.academy.service.impl.ProductParserImpl.class;
            } else if (interfaceClazz == mate.academy.service.ProductService.class) {
                return mate.academy.service.impl.ProductServiceImpl.class;
            }
            throw new RuntimeException("No implementation found for " + interfaceClazz.getName());
        }
        return interfaceClazz;
    }
}
