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

    public Object getInstance(Class<?> interfaceClazze) {
        Object clazzImplInstance = null;
        Class<?> clazz = findImpl(interfaceClazze);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInst = getInstance(field.getType());
                clazzImplInstance = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplInstance, fieldInst);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value", e);
                }
            }
        }
        if (clazzImplInstance == null) {
            clazzImplInstance = createNewInstance(clazz);
        }
        return clazzImplInstance;
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
        } catch (NoSuchMethodException | IllegalAccessException
                | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException("Can't create a new instance :( ");
        }
    }

    private Class<?> findImpl(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplMap = new HashMap<>();
        interfaceImplMap.put(ProductService.class, ProductServiceImpl.class);
        interfaceImplMap.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        Class<?> implClazz = interfaceClazz;
        if (implClazz.isInterface()) {
            implClazz = interfaceImplMap.get(interfaceClazz);
        }
        if (implClazz.isAnnotationPresent(Component.class)) {
            return implClazz;
        }
        throw new RuntimeException("Operation failed");
    }
}
