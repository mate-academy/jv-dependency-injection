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
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementatnion(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed, missing @Component"
                    + "annotaion on the class " + clazz.getName());
        }
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldIntsance = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldIntsance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to initialize field " + field.getName()
                    + "in class" + clazz.getName());
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
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
            throw new RuntimeException("Unnable to create instance of " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementatnion(Class<?> interfaceClazz) {
        Map<Class<?>,Class<?>> intefaceImplementations = new HashMap<>();
        intefaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        intefaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        intefaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        return interfaceClazz.isInterface()
                ? intefaceImplementations.get(interfaceClazz) : interfaceClazz;
    }
}
