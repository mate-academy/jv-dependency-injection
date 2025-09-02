package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Class<?>> interfaceImplementations;
    private final Map<Class<?>, Object> cachedInstances;

    private Injector() {
        interfaceImplementations = Map.of(
                FileReaderService.class, FileReaderServiceImpl.class,
                ProductParser.class, ProductParserImpl.class,
                ProductService.class, ProductServiceImpl.class
        );
        cachedInstances = new HashMap<>();
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            throw new RuntimeException("Class "
                    + interfaceClazz.getName()
                    + " is not an interface.");
        }

        Class<?> implementationClass = interfaceImplementations.get(interfaceClazz);
        if (implementationClass == null) {
            throw new RuntimeException("No implementation found for interface "
                    + interfaceClazz.getName());
        }

        try {
            return createInstance(implementationClass);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Injection failed for class "
                    + implementationClass.getName(), e);
        }
    }

    private Object createInstance(Class<?> implementationClass)
            throws ReflectiveOperationException {
        if (cachedInstances.containsKey(implementationClass)) {
            return cachedInstances.get(implementationClass);
        }

        if (implementationClass.isAnnotationPresent(Component.class)) {
            Object instance = implementationClass.getConstructor().newInstance();
            cachedInstances.put(implementationClass, instance);

            for (Field field : Stream.of(implementationClass.getDeclaredFields())
                    .filter(f -> f.isAnnotationPresent(Inject.class))
                    .collect(Collectors.toList())) {
                field.setAccessible(true);
                Object fieldInstance = createInstance(interfaceImplementations
                        .get(field.getType()));
                field.set(instance, fieldInstance);
            }
            return instance;
        } else {
            throw new RuntimeException("Missing @Component annotation on the class "
                    + implementationClass.getName());
        }
    }
}
