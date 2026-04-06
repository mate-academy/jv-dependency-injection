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
    private static final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);

        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz.getName()
                    + " is not annotated with @Component");
        }
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        Object clazzImplementationInstance = createNewInstance(clazz);;

        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't inject dependency into field "
                            + field.getName(), e);
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        Constructor<?> constructor;
        try {
            constructor = clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Can't get constructor for class " + clazz.getName(), e);
        }
        Object instance;
        try {
            instance = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Can't create instance of class "
                    + clazz.getName(), e);
        }
        instances.put(clazz, instance);
        return instance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementation = new HashMap<>();
        interfaceImplementation.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementation.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementation.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            Class<?> implementation = interfaceImplementation.get(interfaceClazz);
            if (implementation == null) {
                throw new RuntimeException("No implementation found for "
                        + interfaceClazz.getName());
            }
            return implementation;
        }
        return interfaceClazz;
    }
}
