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
        Field[] declaredField = clazz.getDeclaredFields();

        for (Field field: declaredField) {

            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value."
                            + "Class: " + clazz.getName()
                            + ". Field: " + field.getName() + e);
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

            if (!constructor.canAccess(null)) {
                constructor.canAccess(true);
            }
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Class " + clazz.getName()
                    + " must have no-argument constructor", e);
        } catch (InvocationTargetException
                 | InstantiationException
                 | IllegalAccessException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName() + e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);

        Class<?> clazzImplementation = interfaceClazz;

        if (interfaceClazz.isInterface()) {
            clazzImplementation = interfaceImplementations.get(interfaceClazz);

            if (clazzImplementation == null) {
                throw new RuntimeException("There is no implementation for: " + interfaceClazz);
            }
        }

        if (!clazzImplementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't create a new instance of " + clazzImplementation);
        }
        return clazzImplementation;
    }
}
