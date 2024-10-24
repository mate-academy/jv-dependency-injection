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
    private final Map<Class<?>, Class<?>> implementationsMap = new HashMap<>();

    private Injector() {
        implementationsMap.put(ProductService.class, ProductServiceImpl.class);
        implementationsMap.put(ProductParser.class, ProductParserImpl.class);
        implementationsMap.put(FileReaderService.class, FileReaderServiceImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (clazz == null) {
            throw new RuntimeException("No implementation found for class: "
                    + interfaceClazz.getName());
        }
        return createInstance(clazz);
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (implementationsMap.containsKey(interfaceClazz)) {
            return implementationsMap.get(interfaceClazz);
        }
        return null;
    }

    private Object createInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            injectDependencies(instance);

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName(), e);
        }
    }

    private void injectDependencies(Object instance) {
        Field[] declaredFields = instance.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                try {
                    Object fieldInstance = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class: "
                            + instance.getClass().getName() + ". Field: " + field.getName(), e);
                }
            }
        }
    }
}
