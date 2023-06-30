package mate.academy.lib;

import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();
    private Map<Class<?>, Object> existingInstances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);

        Field[] declaredFields = interfaceClazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object filedObject = getInstance(field.getClass());
            }
        }
        return null;
    }

    private Class<?> findImplementation(Class<?> clazz) {
        Map<Class<?>, Class<?>> implementations = new HashMap<>();
        implementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementations.put(ProductParser.class, ProductParserImpl.class);
        implementations.put(ProductService.class, ProductServiceImpl.class);
        if (!clazz.isInterface()) {
            return clazz;
        }
        if (implementations.containsKey(clazz)) {
            return implementations.get(clazz);
        }
        throw new RuntimeException("There is no implementation for this class: " + clazz);
    }

    private Object createNewInstance(Class<?> clazz) {
        Object newInstance;
        if (existingInstances.containsKey(clazz)) {
            return existingInstances.get(clazz);
        }
        try {
            newInstance = clazz.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(String.format("Can't create new instance of %s class", clazz));
        }
        existingInstances.put(clazz, newInstance);
    }
}
