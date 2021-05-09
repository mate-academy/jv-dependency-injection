package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClass) {
        Object classImplementationInstance = null;
        Class<?> type = findImplementation(interfaceClass);
        Field[] declaredFields = type.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplementationInstance = createNewInstance(type);
                field.setAccessible(true);
                try {
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Unable to initialize field value. Class:"
                            + type.getName() + ". Field " + field.getName());
                }
            }
        }

        if (classImplementationInstance == null) {
            classImplementationInstance = createNewInstance(type);
        }
        return classImplementationInstance;
    }

    private Object createNewInstance(Class<?> type) {
        if (instances.containsKey(type)) {
            return instances.get(type);
        }
        try {
            Constructor<?> constructor = type.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(type, instance);
            return instance;
        } catch (NoSuchMethodException | InvocationTargetException
                | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Can`t create instance of " + type.getName());
        }

    }

    private Class<?> findImplementation(Class<?> interfaceClass) {
        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);

        if (interfaceClass.isInterface()) {
            Class<?> implementation = interfaceImplementations.get(interfaceClass);
            if (implementation.isAnnotationPresent(Component.class)) {
                return implementation;
            }
            throw new RuntimeException(implementation.getName() + "is not a Component");
        }
        return interfaceClass;
    }
}
