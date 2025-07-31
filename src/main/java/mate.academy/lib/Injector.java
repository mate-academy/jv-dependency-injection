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

    private final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implClazz = interfaceImplementations.get(interfaceClazz);

        if (implClazz == null) {
            throw new RuntimeException("No implemetation class found for interface: "
                    + interfaceClazz.getName());
        }

        if (!implClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Missing @Component annotation on class "
                    + implClazz.getName()
                    + ". Injection failed");
        }

        if (instances.containsKey(implClazz)) {
            return instances.get(implClazz);
        }

        try {
            Object instance = implClazz.getDeclaredConstructor().newInstance();

            for (Field field : implClazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Class<?> dependencyType = field.getType();
                    Object dependency = getInstance(dependencyType);

                    field.setAccessible(true);
                    field.set(instance, dependency);
                }
            }
            instances.put(implClazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Injection failed for class "
                    + implClazz.getName(), e);
        }
    }
}
