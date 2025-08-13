package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();

    private static final Map<Class<?>, Class<?>> interfaceToImpl = Map.of(
            mate.academy.service.FileReaderService.class, mate.academy.service
                    .impl.FileReaderServiceImpl.class,
            mate.academy.service.ProductParser.class, mate.academy.service
                    .impl.ProductParserImpl.class,
            mate.academy.service.ProductService.class, mate.academy.service
                    .impl.ProductServiceImpl.class
    );

    private final Map<Class<?>, Object> instances = new HashMap<>();

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }

        Class<?> implClazz = interfaceToImpl.get(interfaceClazz);
        if (implClazz == null) {
            throw new RuntimeException("No implementation found for: "
                    + interfaceClazz.getName());
        }

        if (!implClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Missing @Component annotation on class: "
                    + implClazz.getName());
        }

        Object instance = createInstance(implClazz);
        injectDependencies(instance, implClazz);
        instances.put(interfaceClazz, instance);
        return instance;
    }

    private Object createInstance(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of: "
                    + clazz.getName(), e);
        }
    }

    private void injectDependencies(Object instance, Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object dependency = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(instance, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't inject dependency: "
                            + field.getName(), e);
                }
            }
        }
    }
}
