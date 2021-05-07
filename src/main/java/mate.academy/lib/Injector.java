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
        Class<?> clazz = findImplementation(interfaceClass);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class has no annotation: " + clazz.getName());
        }
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Component.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplementationInstance = createNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can not initialize field value. Class: "
                            + clazz.getName()
                            + ". Field: " + field.getName());
                }
            }
        }
        if (classImplementationInstance == null) {
            classImplementationInstance = createNewInstance(clazz);
        }
        return classImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (InstantiationException | InvocationTargetException
                | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Can not create a new insatnce of " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClass) {
        Map<Class<?>, Class<?>> interFaceImplementations = new HashMap<>();
        interFaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        interFaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interFaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        if (interfaceClass.isInterface()) {
            return interFaceImplementations.get(interfaceClass);
        }
        return interfaceClass;
    }
}
