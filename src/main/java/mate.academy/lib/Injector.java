package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private static Map<Class<?>, Class<?>> interfaceImplementations;
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        if (interfaceImplementations == null) {
            Logger logger = Logger.getLogger(Injector.class.getName());
            logger.warning("Now the injector uses the default interfaceImplementations Map.\n"
                    + "Please use `getInjector(Map<Class<?>, Class<?>> interfaceImplementations)`\n"
                    + "to initialize the interfaceImplementations Map");
        }
        interfaceImplementations = Map.of(
                FileReaderService.class, FileReaderServiceImpl.class,
                ProductParser.class, ProductParserImpl.class,
                ProductService.class, ProductServiceImpl.class
        );
        return injector;
    }

    public static Injector getInjector(Map<Class<?>, Class<?>> interfaceImplementations) {
        Injector.interfaceImplementations = interfaceImplementations;
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz.getName()
                    + " not marked with an annotation Component");
        }
        Object clazzImplementationInstance = createNewInstanceOrGetExisting(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. "
                            + "Class: " + clazz.getName() + ". Field: " + field.getName(), e);
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstanceOrGetExisting(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            if (!interfaceImplementations.containsKey(interfaceClazz)) {
                throw new RuntimeException("There is no such implementation for " + interfaceClazz);
            }
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
