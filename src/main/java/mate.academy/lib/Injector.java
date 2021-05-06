package mate.academy.lib;

import mate.academy.exception.WrongInitializingException;
import mate.academy.exception.WrongInstanceCreatingException;
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
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceInstance) {
        Object classImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceInstance);
        Field[] declaredFields = clazz.getDeclaredFields();
        if (clazz.isAnnotationPresent(Component.class)) {
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    classImplementationInstance = createNewInstance(clazz);
                    try {
                        field.setAccessible(true);
                        field.set(classImplementationInstance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Can't initialize field " + field.getName());
                    }
                }
            }
        } else {
            throw new WrongInitializingException("Class " + clazz.getName()
                    + " without annotation");
        }
        if (classImplementationInstance == null) {
            classImplementationInstance = createNewInstance(clazz);
        }
        return classImplementationInstance;
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
        } catch (NoSuchMethodException | InvocationTargetException
                | InstantiationException | IllegalAccessException e) {
            throw new WrongInstanceCreatingException("Can't create an instance of "
                    + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceInstance) {
        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceInstance.isInterface()) {
            return interfaceImplementations.get(interfaceInstance);
        } else {
            return interfaceInstance;
        }
    }
}
