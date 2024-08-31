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
    private final Map<Class<?>, Class<?>> interfaceImplementationsClasses = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }
        validateImplementationPresent(interfaceClazz);
        Class<?> implementationClass = interfaceImplementationsClasses.get(interfaceClazz);
        validateComponentPresent(implementationClass);
        Object instance = newInstance(implementationClass);
        Field[] fields = implementationClass.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(Inject.class)) {
                continue;
            }
            Object fieldValue = getInstance(field.getType());
            setFieldValue(instance, field, fieldValue);
        }

        return instance;
    }

    private static void setFieldValue(Object instance, Field field, Object fieldValue) {
        try {
            field.setAccessible(true);
            field.set(instance, fieldValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to initialize field value", e);
        }
    }

    private static void validateComponentPresent(Class<?> implementationClass) {
        if (!implementationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Target instance should have @Component present");
        }
    }

    private void validateImplementationPresent(Class<?> interfaceClazz) {
        if (!interfaceImplementationsClasses.containsKey(interfaceClazz)) {
            throw new RuntimeException(String.format("No implementation for interface %s",
                    interfaceClazz.getName()));
        }
    }

    private Object newInstance(Class<?> implementationClass) {
        try {
            return implementationClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("An error occurred while creating a new instance", e);
        }
    }
}
