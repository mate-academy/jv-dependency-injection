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

    private final Map<Class<?>, Class<?>> interfaceImplementationMap = Map.of(
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class
    );

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }

        Class<?> implementationClass = findImplementation(interfaceClazz);

        if (!implementationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    implementationClass.getSimpleName()
                            + " is not annotated with @Component"
            );
        }

        try {
            Object instance = implementationClass.getDeclaredConstructor().newInstance();

            for (Field field : implementationClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object dependency = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(instance, dependency);
                }
            }

            instances.put(interfaceClazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Can't create instance of " + implementationClass.getSimpleName(),
                    e
            );
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Class<?> implementation = interfaceImplementationMap.get(interfaceClazz);

        if (implementation == null) {
            throw new RuntimeException(
                    "No implementation found for " + interfaceClazz.getSimpleName()
            );
        }

        return implementation;
    }
}
