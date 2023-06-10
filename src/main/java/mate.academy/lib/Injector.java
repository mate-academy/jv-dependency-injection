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
    private static Injector instance;
    private Map<Class<?>, Object> instances;

    private Injector() {
        instances = new HashMap<>();
        initializeInstances();
    }

    public static Injector getInjector() {
        if (instance == null) {
            instance = new Injector();
        }
        return instance;
    }

    public <T> T getInstance(Class<T> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = interfaceClazz.getDeclaredFields();
        Object instance = createNewInstance(clazz);
        injectFields(instance, declaredFields);
        return interfaceClazz.cast(instance);
    }
    //

    private void initializeInstances() {
        instances.put(FileReaderService.class, new FileReaderServiceImpl());
        instances.put(ProductParser.class, new ProductParserImpl());
        instances.put(ProductService.class, new ProductServiceImpl());
    }

    private <T> Class<?> findImplementation(Class<T> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        return interfaceImplementations.get(interfaceClazz);
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName(), e);
        }
    }

    private void injectFields(Object instance, Field[] fields) {
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Class<?> fieldType = field.getType();
                Object fieldInstance = getInstance(fieldType);
                field.setAccessible(true);
                try {
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error injecting field: " + field.getName(), e);
                }
            }
        }
    }
}
