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
        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzImplementationInstance = createNewInstance(clazz);
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                if (!clazz.isAnnotationPresent(Component.class)) {
                    throw new RuntimeException("Class " + clazz.getName()
                            + " must be annotated with @Component to inject field "
                            + field.getName());
                }
                Object fieldInstance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't set field " + field.getName()
                            + " in class " + clazz.getName(), e);
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
            Constructor<?> constructors = clazz.getConstructor();
            Object instance = null;
            instance = constructors.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                 | NoSuchMethodException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementation = new HashMap<>();
        interfaceImplementation.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementation.put(ProductService.class, ProductServiceImpl.class);
        interfaceImplementation.put(FileReaderService.class, FileReaderServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            Class<?> implementation = interfaceImplementation.get(interfaceClazz);
            if (implementation == null) {
                throw new RuntimeException("No implementation found for interface: "
                        + interfaceClazz.getName());
            }
            return implementation;
        }
        if (!interfaceImplementation.containsValue(interfaceClazz)) {
            throw new RuntimeException("Unsupported class: " + interfaceClazz.getName());
        }
        return interfaceClazz;
    }
}
