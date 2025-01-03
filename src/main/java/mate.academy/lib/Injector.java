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
        if (instances.containsKey(clazz)) {
            return (T) instances.get(clazz);
        }

        try {
            T instance = createInstance(clazz);
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create instance for " + clazz.getName(), e);
        }
    }

    private <T> T createInstance(Class<T> clazz) throws ReflectiveOperationException {
        // If clazz is an interface, get the mapped implementation
        Class<?> mappedClass = mappings.getOrDefault(clazz, clazz);

        // Look for a constructor with @Inject annotation
        for (Constructor<?> constructor : mappedClass.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                Object[] parameters = new Object[parameterTypes.length];

                // Inject dependencies by recursively calling getInstance
                for (int i = 0; i < parameterTypes.length; i++) {
                    parameters[i] = getInstance(parameterTypes[i]);
                }

                constructor.setAccessible(true);
                return (T) constructor.newInstance(parameters);
            }
        }

        // If no constructor with @Inject annotation, fallback to default constructor
        return (T) mappedClass.getDeclaredConstructor().newInstance();
    }
}
