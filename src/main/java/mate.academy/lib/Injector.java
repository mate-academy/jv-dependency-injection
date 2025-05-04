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
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = creatNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cant initialize field. Class: "
                            + clazz.getName() + " . Field" + field.getName());
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = creatNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private Object creatNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object newInstance = constructor.newInstance();
            instances.put(clazz, newInstance);
            return newInstance;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            throw new RuntimeException("Cant creat new instance" + clazz.getName());
        }

    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImpl = new HashMap<>();
        interfaceImpl.put(ProductService.class, ProductServiceImpl.class);
        interfaceImpl.put(ProductParser.class, ProductParserImpl.class);
        interfaceImpl.put(FileReaderService.class, FileReaderServiceImpl.class);
        Class<?> clazz = interfaceClazz.isInterface() ? interfaceImpl.get(interfaceClazz)
                : interfaceClazz;
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class without Component");
        }
        return clazz;
    }
}
