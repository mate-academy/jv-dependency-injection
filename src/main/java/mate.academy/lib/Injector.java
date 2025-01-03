package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> mappings = new HashMap<>();

    private Injector() {
        // Manually map interfaces to concrete classes
        mappings.put(mate.academy.service.ProductService.class,
                mate.academy.service.impl.ProductServiceImpl.class);
        mappings.put(mate.academy.service.FileReaderService.class,
                mate.academy.service.impl.FileReaderServiceImpl.class);
        mappings.put(mate.academy.service.ProductParser.class,
                mate.academy.service.impl.ProductParserImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public <T> T getInstance(Class<T> clazz) {
        // Check if instance already exists
        if (instances.containsKey(clazz)) {
            return (T) instances.get(clazz);
        }

        try {
            T instance = createInstance(clazz);
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create instance for " + clazz.getName(), e);
        } catch (Exception e) {
            // Ensure we throw a RuntimeException if no suitable constructor or mapping is found
            throw new RuntimeException("Unsupported class: " + clazz.getName(), e);
        }
    }

    private <T> T createInstance(Class<T> clazz) throws ReflectiveOperationException {
        // Resolve to mapped class if it's an interface
        Class<?> mappedClass = mappings.getOrDefault(clazz, clazz);

        if (mappedClass == null) {
            throw new RuntimeException("Unsupported class: " + clazz.getName());
        }

        // Try to find the constructor with @Inject annotation
        for (Constructor<?> constructor : mappedClass.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                // Inject dependencies into the constructor
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                Object[] parameters = new Object[parameterTypes.length];

                for (int i = 0; i < parameterTypes.length; i++) {
                    parameters[i] = getInstance(parameterTypes[i]);
                }

                constructor.setAccessible(true);
                T instance = (T) constructor.newInstance(parameters);

                // Inject fields that are annotated with @Inject
                injectFields(instance);

                return instance;
            }
        }

        // If no @Inject constructor, fallback to default constructor
        T instance = (T) mappedClass.getDeclaredConstructor().newInstance();

        // Inject fields if no @Inject constructor
        injectFields(instance);

        return instance;
    }

    private void injectFields(Object instance) throws IllegalAccessException {
        for (var field : instance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                Object fieldInstance = getInstance(field.getType());
                field.set(instance, fieldInstance); // Inject the field
            }
        }
    }
}
