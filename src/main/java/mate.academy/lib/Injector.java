package mate.academy.lib;

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
    private static final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzInstance = createNewInstance(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                try {
                    field.setAccessible(true);
                    field.set(clazzInstance, getInstance(field.getType()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to initialize field " + field.getName());
                }
            }
        }
        return clazzInstance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementationMap = new HashMap<>();
        interfaceImplementationMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementationMap.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementationMap.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return interfaceImplementationMap.get(interfaceClazz);
        }
        return interfaceClazz;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Couldn't create instance of "
                    + clazz.getName() + " there is no "
                    + Component.class.getName() + " annotation.");
        }
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Object instance = clazz.getConstructor().newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (InstantiationException | NoSuchMethodException
                | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Couldn't create instance of " + clazz.getName());
        }
    }
}
