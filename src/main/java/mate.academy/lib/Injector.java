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
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> interfaceImplementation = new HashMap<>();

    {
        interfaceImplementation.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementation.put(ProductService.class, ProductServiceImpl.class);
        interfaceImplementation.put(ProductParser.class, ProductParserImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzImplInstance = createNewInstance(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                try {
                    field.set(clazzImplInstance, getInstance(field.getType()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t initialize field " + field.getName());
                }
            }
        }
        return clazzImplInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (clazz == null) {
            throw new RuntimeException("Could not find implementation of the interface.");
        }
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class have not the annotation @Component!");
        }
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            throw new RuntimeException("Can't instantiate of " + clazz.getName() + ", " + e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        return interfaceClazz.isInterface() ? interfaceImplementation
                .get(interfaceClazz) : interfaceClazz;
    }
}
