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
        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzObject = null;
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't create an instance of " + clazz.getName()
                    + " because @Component annotation is missing above the class");
        }
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldObject = getInstance(field.getType());
                clazzObject = getNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzObject, fieldObject);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't create an instance of class: "
                            + clazz.getName() + ", field: " + field.getName(), e);
                }
            }
        }
        if (clazzObject == null) {
            clazzObject = getNewInstance(clazz);
        }
        return clazzObject;
    }

    private Object getNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object newInstance = constructor.newInstance();
            instances.put(clazz, newInstance);
            return newInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create new instance of " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> clazz) {
        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        return interfaceImplementations.get(clazz);
    }
}
