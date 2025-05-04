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
    private static Map<Class<?>, Object> instances = new HashMap<>();
    private static Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Object classImplInstance = null;
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed, missing "
                    + "@Component annotation on the class "
                    + interfaceClazz.getName());
        }
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field: declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplInstance = getOrCreateNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(classImplInstance,fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. "
                            + "Class" + clazz.getName()
                            + ". Field: " + field.getName());
                }
            }
        }
        if (classImplInstance == null) {
            classImplInstance = getOrCreateNewInstance(clazz);
        }
        return classImplInstance;
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
            throw new RuntimeException("Can't create new instance of  " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            if (interfaceImplementations.get(interfaceClazz) == null) {
                throw new RuntimeException("The implementation for the interface"
                        + interfaceClazz.getName() + " was not found.");
            }
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
