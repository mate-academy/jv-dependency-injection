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
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> inputType) {
        Class<?> inputImplClass = getImplementation(inputType);
        Object interfaceImplementation = null;
        if (inputImplClass.isAnnotationPresent(Component.class)) {
            Field[] allFields = inputImplClass.getDeclaredFields();
            for (Field field : allFields) {
                Class<?> fieldType = field.getType();
                Class<?> fieldImplClass = getImplementation(fieldType);
                if (fieldImplClass.isAnnotationPresent(Component.class)) {
                    Object fieldInstance = getInstance(fieldImplClass);
                    interfaceImplementation = createNewInstance(inputImplClass);

                    try {
                        field.setAccessible(true);
                        field.set(interfaceImplementation, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Can't initialize field value. Class "
                                + inputType + ". Field " + field + e);
                    }
                }
            }
            if (interfaceImplementation == null) {
                interfaceImplementation = createNewInstance(inputImplClass);
            }
        }
        return interfaceImplementation;
    }

    public Class<?> getImplementation(Class<?> inputType) {
        Map<Class<?>, Class<?>> findImplementation = new HashMap<>();
        findImplementation.put(FileReaderService.class, FileReaderServiceImpl.class);
        findImplementation.put(ProductParser.class, ProductParserImpl.class);
        findImplementation.put(ProductService.class, ProductServiceImpl.class);
        if (inputType.isInterface()) {
            return findImplementation.get(inputType);
        }
        return inputType;
    }

    public Object createNewInstance(Class<?> inputImplClass) {
        if (instances.containsKey(inputImplClass)) {
            return instances.get(inputImplClass);
        }
        try {
            Constructor<?> constructor = inputImplClass.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(inputImplClass, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + inputImplClass + e);
        }
    }
}
