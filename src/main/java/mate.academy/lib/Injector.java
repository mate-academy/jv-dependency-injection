package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import mate.academy.exceptions.MissedRequiredAnnotationException;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private Map<Class<?>, Object> instances = new HashMap<>();
    private Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (!interfaceClazz.isAnnotationPresent(Component.class)) {
            throw new MissedRequiredAnnotationException("To create instance we need "
                    + "to have Component annotation");
        }
        Object classImplInstance = null;
        Class<?> clazz = findImplementationFor(interfaceClazz);
        Field[] declaredFields = interfaceClazz.getDeclaredFields();

        for (var field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplInstance = getOrCreateNewInstance(clazz);

                try {
                    field.setAccessible(true);
                    field.set(classImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't init field value. Class: "
                            + clazz.getName() + ". Field: " + field.getName());
                }
            }
        }

        if (classImplInstance == null) {
            classImplInstance = getOrCreateNewInstance(clazz);
        }
        return classImplInstance;
    }

    private Object getOrCreateNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of" + clazz.getName());
        }
    }

    private Class<?> findImplementationFor(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
