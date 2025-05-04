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
    private static final Map<Class<?>, Object> instances = new HashMap<>();
    private static final Map<Class<?>, Class<?>> interfaceImplementations
            = Map.of(ProductService.class, ProductServiceImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplInstance = null;
        Class<?> clazz = findImpl(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can`t create new instance of this class "
                    + clazz.getName());
        }
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplInstance = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t initialize field value. "
                            + "Class: " + clazz.getName() + " Field: " + field.getName());
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
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can`t create new instance of " + clazz.getName());
        }
    }

    private Class<?> findImpl(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
