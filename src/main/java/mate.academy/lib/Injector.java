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
    private static final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object implementationInstance = null;
        Class<?> clazzImplementation = findImplementation(interfaceClazz);
        Field[] declaredFields = interfaceClazz.getDeclaredFields();
        if (!clazzImplementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed, missing @Component annotation "
                    + "on the class" + clazzImplementation);
        }
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object instance = getInstance(field.getType());
                implementationInstance = createInstance(clazzImplementation);
                try {
                    field.setAccessible(true);
                    field.set(implementationInstance, instance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value: Class:"
                            + clazzImplementation.getName() + ". Field: " + field);
                }
            }
        }
        if (implementationInstance == null) {
            implementationInstance = createInstance(clazzImplementation);
        }
        return implementationInstance;
    }

    private Object createInstance(Class<?> clazzImplementation) {
        if (instances.containsKey(clazzImplementation)) {
            return instances.get(clazzImplementation);
        }
        try {
            Constructor<?> constructor = clazzImplementation.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazzImplementation, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of class: "
                    + clazzImplementation, e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
