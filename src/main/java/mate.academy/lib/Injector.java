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
    private static final Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    static {
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
    }

    private Injector() {

    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        return createOrGetInstance(interfaceClazz);
    }

    private Object createOrGetInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }
        Class<?> implementationClass = interfaceImplementations
                .getOrDefault(interfaceClazz, interfaceClazz);
        if (!implementationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(Component.class + " annotation missing. "
                    + "Can't create an instance of class " + implementationClass.getName());
        }
        Object instance = createNewInstance(implementationClass);
        injectDependencies(instance);
        instances.put(interfaceClazz, instance);
        return instance;
    }

    private Object createNewInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName(), e);
        }
    }

    private void injectDependencies(Object instance) {
        Field[] declaredFields = instance.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Class<?> fieldType = field.getType();
                Object fieldInstance = createOrGetInstance(fieldType);
                field.setAccessible(true);
                try {
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. "
                            + "Class: " + instance.getClass().getName()
                            + ". Field: " + field.getName(), e);
                }
            }
        }
    }
}
