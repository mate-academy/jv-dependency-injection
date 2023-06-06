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
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        checkComponentAnnotationPresent(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field: declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = getOrCreateNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. "
                            + "Class: " + clazz.getName() + ". Field: "
                            + field.getName(), e);
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = getOrCreateNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private Object getOrCreateNewInstance(Class<?> clazz) {
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

    private void checkComponentAnnotationPresent(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Can't create an instance of class that haven't @Component annotation");
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            if (interfaceImplementations.containsKey(interfaceClazz)) {
                return interfaceImplementations.get(interfaceClazz);
            } else {
                throw new RuntimeException("There is no implementation for "
                        + interfaceClazz.getName() + " interface");
            }
        }
        return interfaceClazz;
    }
}
