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

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = getClassImplementation(interfaceClazz);
        Object clazzInstance = createClassInstance(clazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("You're not able to create instance of "
                    + clazz.getName() + " class");
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field: fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(clazzInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't set field: " + field.getName()
                            + " in " + clazz.getName() + " class.", e);
                }
            }
        }
        return clazzInstance;
    }

    private Object createClassInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object newInstance = constructor.newInstance();
            instances.put(clazz, newInstance);
            return newInstance;
        } catch (NoSuchMethodException | InstantiationException
                | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Can't create an instance of " + clazz.getName(), e);
        }
    }

    private Class<?> getClassImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImpls = new HashMap<>();
        interfaceImpls.put(ProductService.class, ProductServiceImpl.class);
        interfaceImpls.put(ProductParser.class, ProductParserImpl.class);
        interfaceImpls.put(FileReaderService.class, FileReaderServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return interfaceImpls.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}

