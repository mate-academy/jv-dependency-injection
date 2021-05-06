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
        Class<?> classImplementation = findInterfaceImplementation(interfaceClass);
        Field[] declaredFields = classImplementation.getDeclaredFields();
        Object classImplementationInstance = null;
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplementationInstance = createNewInstance(classImplementation);
                field.setAccessible(true);
                try {
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot set field: " + field.getName()
                            + " in class: " + interfaceClass.getName());
                }
            }
        }
        if (classImplementationInstance == null) {
            classImplementationInstance = createNewInstance(classImplementation);
        }
        return classImplementationInstance;
    }

    private Object createNewInstance(Class<?> classImplementation) {
        if (instances.containsKey(classImplementation)) {
            return instances.get(classImplementation);
        }
        try {
            Constructor<?> constructor = classImplementation.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(classImplementation, instance);
            return instance;
        } catch (NoSuchMethodException | InvocationTargetException
                | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Cannot create instance of "
                    + classImplementation.getName());
        }
    }

    private Class<?> findInterfaceImplementation(Class<?> interfaceClass) {
        Map<Class<?>, Class<?>> interfaceImplementationMap = new HashMap<>();
        interfaceImplementationMap.put(ProductService.class, ProductServiceImpl.class);
        interfaceImplementationMap.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementationMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        Class<?> implementationClass = interfaceClass;
        if (implementationClass.isInterface()) {
            implementationClass = interfaceImplementationMap.get(interfaceClass);
        }
        if (implementationClass.isAnnotationPresent(Component.class)) {
            return implementationClass;
        }
        throw new RuntimeException("Class" + implementationClass.getName()
                + " does not have 'Component' annotation.");
    }
}
