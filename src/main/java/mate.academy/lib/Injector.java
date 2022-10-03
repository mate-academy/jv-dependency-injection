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

    Map<Class<?>, Object> instances = new HashMap<>();

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null; //result
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = interfaceClazz.getDeclaredFields();
        for (final Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldObject = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldObject);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initiate field value. " +
                            "Class: " + clazz.getName() + ".Field: " + field.getName());
                }
            }
        }

        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private Class<?> findImplementation(final Class<?> interfaceClazz) {
        Map<Class<?>,Class<?>> interfaceImplementation = new HashMap<>();
        interfaceImplementation.put(ProductService.class, ProductServiceImpl.class);
        interfaceImplementation.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementation.put(FileReaderService.class, FileReaderServiceImpl.class);
        if(interfaceClazz.isInterface()){
            return interfaceImplementation.get(interfaceClazz);
        }
        return interfaceClazz;
    }

    private Object createNewInstance(final Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        if(!clazz.isAnnotationPresent(Component.class)){
            throw new RuntimeException("Can't create a new instance of " + clazz.getName());
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz,instance);
            return instance;
        } catch (NoSuchMethodException | InstantiationException |
                IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName());
        }
    }
}
