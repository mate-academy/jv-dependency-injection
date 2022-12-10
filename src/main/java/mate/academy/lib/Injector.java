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

    public Object getInstance(Class<?> interfaceClass) {
        Class<?> inputClass = findImplementation(interfaceClass);
        if (!inputClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Injection failed, missing @Component annotaion " 
            + "on the class " + interfaceClass.getName());
        }
        Field[] declaredFields = inputClass.getDeclaredFields();
        Object inputClassImplementationInstance = null;
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                inputClassImplementationInstance = createNewInstatnce(inputClass);
                try {
                    field.setAccessible(true);
                    field.set(inputClassImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize filed value. Class: " 
                + inputClass.getName()
                            + ", field: " + field.getName(), e);
                }
            }
        }
        if (inputClassImplementationInstance == null) {
            inputClassImplementationInstance = createNewInstatnce(inputClass);
        }
        return inputClassImplementationInstance;
    }

    private Object createNewInstatnce(Class<?> inputClass) {
        if (instances.containsKey(inputClass)) {
            return instances.get(inputClass);
        }
        try {
            Constructor<?> constructor = inputClass.getConstructor();
            Object newInstance = constructor.newInstance();
            instances.put(inputClass, newInstance);
            return newInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " 
        + inputClass.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClass) {
        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClass.isInterface()) {
            return interfaceImplementations.get(interfaceClass);
        }
        return interfaceClass;
    }
}
