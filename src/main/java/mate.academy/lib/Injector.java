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

    public Object getInstance(Class<?> intefaceClass) {
        Object classImplementationInstance = null;
        Class<?> implClass = findImplementation(intefaceClass);
        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Implementation is not supported "
                    + implClass.getSimpleName());
        }
        Field[] declaredFields = implClass.getDeclaredFields();
        for (Field field: declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                classImplementationInstance = createNewInstance(implClass);
                Object fieldInstance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class;"
                            + implClass.getName()
                            + ". Field:"
                            + field.getName(), e);
                }
            }
        }
        if (classImplementationInstance == null) {
            classImplementationInstance = createNewInstance(implClass);
        }
        return classImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        Constructor<?> constructor = null;
        try {
            constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException | IllegalAccessException
                | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException("Can't create a new instance of "
                    + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> intefaceClass) {
        Map<Class<?>, Class<?>> intefaceImplementations = new HashMap<>();
        intefaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        intefaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        intefaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        return intefaceClass.isInterface()
                ? intefaceImplementations.get(intefaceClass)
                : intefaceClass;
    }
}
