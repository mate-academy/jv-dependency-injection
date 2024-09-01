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
    private static final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implClass = getImplementation(interfaceClazz);

        if (instances.containsKey(implClass)) {
            return instances.get(implClass);
        }

        try {
            Object instance = createNewInstance(implClass);
            instances.put(implClass, instance);
            injectDependencies(instance, implClass);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Injection failed, could not instantiate "
                    + implClass.getName(), e);
        }
    }

    private Class<?> getImplementation(Class<?> interfaceClazz) {
        Class<?> implClass = interfaceImplementations.get(interfaceClazz);

        if (implClass == null) {
            implClass = createNewImplementation(interfaceClazz);
        }

        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Injection failed, missing @Component annotation on the class "
                            + implClass.getName());
        }

        return implClass;
    }

    private Class<?> createNewImplementation(Class<?> interfaceClazz) {
        throw new UnsupportedOperationException("Dynamic class creation not implemented yet.");
    }

    private Object createNewInstance(Class<?> implClass)
            throws ReflectiveOperationException {
        return implClass.getDeclaredConstructor().newInstance();
    }

    private void injectDependencies(Object instance, Class<?> implClass)
            throws ReflectiveOperationException {
        for (Field field : implClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                Object dependency = getInstance(field.getType());
                field.set(instance, dependency);
            }
        }
    }
}
