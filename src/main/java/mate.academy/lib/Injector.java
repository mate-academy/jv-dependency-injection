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
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> interfaceImplementation = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationsInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declareFields = clazz.getDeclaredFields();
        for (Field field : declareFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object findInstance = getInstance(field.getType());
                clazzImplementationsInstance = createNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationsInstance, findInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t initialize field value. Class: "
                            + clazz.getName() + " Field. " + field.getName());
                }
            }
        }
        if (clazzImplementationsInstance == null) {
            clazzImplementationsInstance = createNewInstance(clazz);
        }
        return clazzImplementationsInstance;
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
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException
            | InvocationTargetException e) {
            throw new RuntimeException("Can`t create new instance of " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        interfaceImplementation.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementation.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementation.put(ProductService.class, ProductServiceImpl.class);
        if (!interfaceClazz.isAnnotationPresent(Component.class) && !interfaceClazz.isInterface()) {
            throw new RuntimeException("Class don`t have annotation mark!"
                    + interfaceClazz);
        }
        if (interfaceClazz.isInterface()) {
            return interfaceImplementation.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
