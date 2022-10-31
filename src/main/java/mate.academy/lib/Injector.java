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
    private static final Map<Class<?>, Class<?>> interfaceImplementationMap = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object classImplementationInstance = null;
        Class<?> implementation = findRealization(interfaceClazz);
        Field[] declaredFields = implementation.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplementationInstance = createInstance(implementation);
                field.setAccessible(true);
                try {
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't inject " + fieldInstance
                            + " in " + field, e);
                }
            }
        }
        if (classImplementationInstance == null) {
            classImplementationInstance = createInstance(implementation);
        }
        return classImplementationInstance;
    }

    private Object createInstance(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Error trying create instance of "
                    + clazz.getName()
                    + " though Injector: missing Component annotation");
        }
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> classConstructor = clazz.getConstructor();
            Object newInstance = classConstructor.newInstance();
            instances.put(clazz, newInstance);
            return newInstance;
        } catch (Exception e) {
            throw new RuntimeException("Can't create object of " + clazz.getName(), e);
        }
    }

    private Class<?> findRealization(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            return interfaceClazz;
        }
        return interfaceImplementationMap.get(interfaceClazz);
    }
}
