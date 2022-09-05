package mate.academy.lib;

import java.lang.reflect.Constructor;
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
    private final Map<Class<?>, Object> classObjectMap = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementationMap = createImplementationMap();
        Class<?> interfaceImplementation = interfaceClazz;
        if (interfaceClazz.isInterface()) {
            interfaceImplementation = interfaceImplementationMap.get(interfaceClazz);
        }
        if (!interfaceImplementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "The class is not marked with an appropriate annotation. "
                    + "Unable to create an object.");
        }
        Field[] declaredFields = interfaceImplementation.getDeclaredFields();
        Object interfaceClazzObject = null;
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldObject = getInstance(field.getType());
                interfaceClazzObject = createNewObject(interfaceImplementation);
                try {
                    field.setAccessible(true);
                    field.set(interfaceClazzObject, fieldObject);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field. Class: "
                            + interfaceClazz.getName()
                            + ". Field: " + field.getName());
                }
            }
        }
        if (interfaceClazzObject == null) {
            interfaceClazzObject = createNewObject(interfaceImplementation);
        }
        return interfaceClazzObject;
    }

    private Object createNewObject(Class<?> interfaceImplementation) {
        if (classObjectMap.containsKey(interfaceImplementation)) {
            return classObjectMap.get(interfaceImplementation);
        }
        try {
            Constructor<?> constructor = interfaceImplementation.getConstructor();
            Object newObject = constructor.newInstance();
            classObjectMap.put(interfaceImplementation, newObject);
            return newObject;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't get an instance of "
                    + interfaceImplementation.getSimpleName());
        }
    }

    private Map<Class<?>, Class<?>> createImplementationMap() {
        Map<Class<?>, Class<?>> map = new HashMap<>();
        map.put(FileReaderService.class, FileReaderServiceImpl.class);
        map.put(ProductParser.class, ProductParserImpl.class);
        map.put(ProductService.class, ProductServiceImpl.class);
        return map;
    }
}
