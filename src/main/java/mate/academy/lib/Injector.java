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
    private final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementationClazz = interfaceImplementations.get(interfaceClazz);
        if (implementationClazz == null) {
            throw new IllegalStateException("No implementation for " + interfaceClazz);
        }
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }
        if (!implementationClazz.isAnnotationPresent(Component.class)) {
            throw new IllegalStateException(
                    "Injection failed, missing @Component annotation on class "
                            + implementationClazz.getName()
            );
        }
        try {
            Object instance = implementationClazz.getDeclaredConstructor().newInstance();
            for (Field field : implementationClazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(instance, fieldInstance);
                }
            }
            instances.put(interfaceClazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Can't create instance of class " + implementationClazz.getName(),
                    e
            );
        }
    }
}
