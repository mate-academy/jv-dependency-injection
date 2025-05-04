package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import mate.academy.impl.FileReaderServiceImpl;
import mate.academy.impl.ProductParserImpl;
import mate.academy.impl.ProductServiceImpl;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;

public class Injector {
    private static final Injector injector = new Injector();
    private static final Map<Class<?>, Class<?>> interfaceImplementations;
    private final Map<Class<?>, Object> instances = new HashMap<>();

    static {
        interfaceImplementations = Map.of(
                FileReaderService.class, FileReaderServiceImpl.class,
                ProductParser.class, ProductParserImpl.class,
                ProductService.class, ProductServiceImpl.class
        );
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementationClazz = findImplementation(interfaceClazz);
        if (!implementationClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Injection failed, missing @Component annotation on the class "
                            + implementationClazz.getSimpleName()
            );
        }

        Object clazzImplementationInstance = createNewInstance(implementationClazz);
        for (Field field : implementationClazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                setInjectedField(implementationClazz, clazzImplementationInstance, field);
            }
        }
        return clazzImplementationInstance;
    }

    private void setInjectedField(Class<?> clazz, Object instance, Field field) {
        try {
            Object fieldInstance = getInstance(field.getType());
            field.setAccessible(true);
            field.set(instance, fieldInstance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(String.format("Can't initialize field %s.%s",
                    clazz.getName(), field.getName()), e
            );
        }
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object newInstance = constructor.newInstance();
            instances.put(clazz, newInstance);
            return newInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Can't create instance of this class: " + clazz.getName(), e
            );
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        return interfaceImplementations.getOrDefault(interfaceClazz, interfaceClazz);
    }
}
