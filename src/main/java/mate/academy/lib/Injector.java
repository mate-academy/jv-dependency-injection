package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClass) {
        if (!interfaceClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Injection failed, missing @Component annotaion on the class " + interfaceClass.getName());
        }
        Class<?> inputClass = findImplementation(interfaceClass);
        Field[] declaredFields = inputClass.getDeclaredFields();
        Object inputClassImplementationInstance = null;
        if (interfaceClass.isAnnotationPresent(Component.class)) {
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    // create a new object of field type if we have recursion dependencies 
                    Object fieldInstance = getInstance(field.getType());
                    // create a new object or interface class or (implementation class)
                    inputClassImplementationInstance = createNewInstatnce(inputClass);
                    // set 'field type object' to 'interfaceClass object'
                    try {
                        field.setAccessible(true);
                        field.set(inputClassImplementationInstance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Can't initialize filed value. Class: " + inputClass.getName()
                                + ", field: " + field.getName(), e);
                    }
                }
            }

        }
        if (inputClassImplementationInstance == null) {
            inputClassImplementationInstance = createNewInstatnce(inputClass);
        }
        return inputClassImplementationInstance;
    }

    private Object createNewInstatnce(Class<?> inputClass) {
        // if object already created - use it
        if (instances.containsKey(inputClass)) {
            return instances.get(inputClass);
        }
        // create a new object
        try {
            Constructor<?> constructor = inputClass.getConstructor();
            Object newInstance = constructor.newInstance();
            instances.put(inputClass, newInstance);
            return newInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + inputClass.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClass) {
        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        if (interfaceClass.isInterface()) {
            return interfaceImplementations.get(interfaceClass);
        }
        return interfaceClass;
    }
}
