package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();

    private final Map<Class<?>, Class<?>> interfaceImplementationMap = Map.of(
            mate.academy.service.FileReaderService.class,
            mate.academy.service.impl.FileReaderServiceImpl.class,
            mate.academy.service.ProductParser.class,
            mate.academy.service.impl.ProductParserImpl.class,
            mate.academy.service.ProductService.class,
            mate.academy.service.impl.ProductServiceImpl.class
    );

    // ✅ CACHE (singleton-like)
    private final Map<Class<?>, Object> instances = new HashMap<>();

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public <T> T getInstance(Class<T> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return (T) instances.get(interfaceClazz);
        }

        Class<?> implementationClass = interfaceImplementationMap.get(interfaceClazz);

        if (implementationClass == null) {
            throw new RuntimeException("No implementation for " + interfaceClazz.getName());
        }

        if (!implementationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class "
                    + implementationClass.getName()
                    + " is not annotated with @Component");
        }

        try {
            Object instance = implementationClass.getDeclaredConstructor().newInstance();

            injectDependencies(instance);

            instances.put(interfaceClazz, instance);

            return (T) instance;
        } catch (ReflectiveOperationException e) { // ✅ poprawione
            throw new RuntimeException("Can't create instance of "
                    + implementationClass.getName(), e);
        }
    }

    private void injectDependencies(Object instance) throws IllegalAccessException {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object dependency = getInstance(field.getType());
                field.setAccessible(true);
                field.set(instance, dependency);
            }
        }
    }
}
