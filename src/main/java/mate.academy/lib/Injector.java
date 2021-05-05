package mate.academy.lib;

import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();

    public static Injector getInjector() {
        return injector;
    }
    Map<Class<?>,Object> instances = new HashMap<>();

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        Object clazzImplementationInstance = null;
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazz);

                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class: "
                    + clazz.getName() + ". Field: " + field.getName());
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        Object instance = null;
        try {
            Constructor<?> constructor = clazz.getConstructor();
             instance = constructor.newInstance();
        } catch (NoSuchMethodException |InvocationTargetException
                | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName(), e);
        }
        instances.put(clazz, instance);
        return instance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isAnnotationPresent(Component.class)) {
            Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
            interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
            interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
            interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
            if (interfaceClazz.isInterface()) {
                return interfaceImplementations.get(interfaceClazz);
            }
            return interfaceClazz;
        }
        throw new RuntimeException("Can't create instance: "
                + interfaceClazz.getName() + " isn't have annotation " + Component.class);
    }
}
