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
    private Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object classImplementationInstance = null;
        Class<?> classInstance = findImplementation(interfaceClazz);
        if (!classInstance.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class is not marked with annotation.");
        }
        Field[] declaredFields = classInstance.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplementationInstance = createNewInstance(classInstance);
                field.setAccessible(true);
                try {
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t initialize field value. "
                            + "Class: " + classInstance.getName()
                            + ". Field: " + field.getName(), e);
                }
            }
        }
        if (classImplementationInstance == null) {
            classImplementationInstance = createNewInstance(classInstance);
        }
        return classImplementationInstance;
    }

    private Object createNewInstance(Class<?> classInstance) {
        if (instances.containsKey(classInstance)) {
            return instances.get(classInstance);
        }

        try {
            Constructor<?> constructor = classInstance.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(classInstance, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can`t create new instance of "
                    + classInstance.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {

        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
