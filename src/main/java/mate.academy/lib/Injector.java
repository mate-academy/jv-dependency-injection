package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            mate.academy.service.ProductParser.class,
            mate.academy.service.impl.ProductParserImpl.class,
            mate.academy.service.FileReaderService.class,
            mate.academy.service.impl.FileReaderServiceImpl.class,
            mate.academy.service.ProductService.class,
            mate.academy.service.impl.ProductServiceImpl.class
    );

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            throw new RuntimeException("Only interfaces can be passed to the Injector");
        }

        Class<?> implementationClazz = findImplementation(interfaceClazz);
        if (!implementationClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("No @Component annotation found for class: "
                    + implementationClazz.getName());
        }

        return createInstance(implementationClazz);
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (!interfaceImplementations.containsKey(interfaceClazz)) {
            throw new RuntimeException("No implementation found for "
                    + interfaceClazz.getName());
        }
        return interfaceImplementations.get(interfaceClazz);
    }

    private Object createInstance(Class<?> implementationClazz) {
        if (instances.containsKey(implementationClazz)) {
            return instances.get(implementationClazz);
        }
        try {
            Constructor<?> constructor = implementationClazz.getDeclaredConstructor();
            if (constructor.getParameterCount() > 0) {
                throw new RuntimeException("No default constructor found in "
                        + implementationClazz.getName());
            }

            Object instance = constructor.newInstance();
            instances.put(implementationClazz, instance);

            for (Field field : implementationClazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    Object fieldInstance = getInstance(field.getType());
                    field.set(instance, fieldInstance);
                }
            }
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create an instance of "
                    + implementationClazz.getName(), e);
        }
    }
}
