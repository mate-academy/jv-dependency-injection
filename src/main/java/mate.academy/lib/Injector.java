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
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplInstance = getClazzImplInstance(interfaceClazz);
        if (clazzImplInstance == null) {
            clazzImplInstance = getOrCreateInstance(findImplementation(interfaceClazz));
        }
        return clazzImplInstance;
    }

    private Object getClazzImplInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = getOrCreateInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't set field value."
                            + "Class: " + clazz.getName() + ". Field: " + field.getName());
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object getOrCreateInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructors = clazz.getConstructor();
            Object instance = constructors.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementation = initializeImplMap();
        throwExceptionIfNotComponent(interfaceClazz);
        if (interfaceClazz.isInterface()) {
            return interfaceImplementation.get(interfaceClazz);
        }
        return interfaceClazz;
    }

    private Map<Class<?>, Class<?>> initializeImplMap() {
        return Map.of(
                ProductService.class, ProductServiceImpl.class,
                ProductParser.class, ProductParserImpl.class,
                FileReaderService.class, FileReaderServiceImpl.class);
    }

    private void throwExceptionIfNotComponent(Class<?> interfaceClazz) {
        if (!interfaceClazz.isAnnotationPresent(Component.class) && !interfaceClazz.isInterface()) {
            throw new RuntimeException("Injection failed, missing @Component annotation"
                    + " on the class: " + interfaceClazz);
        }
    }
}
