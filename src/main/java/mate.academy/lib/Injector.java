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

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        if (clazz == null) {
            throw new RuntimeException("Unsupported class:" + interfaceClazz.getName());
        }
        Field[] declaredFields = interfaceClazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
            }

            clazzImplementationInstance = createNewInstance(clazz);
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;

    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            instances.get(clazz);
        }
        Constructor<?> constructor = null;
        try {
            constructor = clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        Object instance = null;
        try {
            instance = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Can't create a new instance of" + clazz.getName());
        }
        instances.put(clazz, instance);
        return instance;
    }

    private static Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> intarfaceImplementations = new HashMap<>();
        intarfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        intarfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        intarfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return intarfaceImplementations.get(interfaceClazz);
        }
        if (!intarfaceImplementations.containsValue(interfaceClazz)) {
            return null;
        }
        return interfaceClazz;
    }
}
