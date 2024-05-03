package mate.academy.lib;

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
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class
    );

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzImplementationInstance = getImplementationInstance(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class: "
                            + clazz.getName()
                            + "Field: " + field.getName(), e);
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object getImplementationInstance(Class<?> clazz) {
        if (!instances.containsKey(clazz)) {
            try {
                Object instance = clazz.getConstructor().newInstance();
                instances.put(clazz, instance);
                return instance;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Can't create new instance of " + clazz.getName(), e);
            }
        } else {
            return instances.get(clazz);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Class<?> interfaceImplementation = (interfaceClazz.isInterface())
                ? interfaceImplementations.get(interfaceClazz)
                : interfaceClazz;
        if (!interfaceImplementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Injection failed, missing @Component annotation on the class "
                    + interfaceClazz.getName());
        }
        return interfaceImplementation;
    }
}
