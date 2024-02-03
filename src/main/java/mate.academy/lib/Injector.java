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
    private final Map<Class<?>, Class<?>> interfaceImplementations;
    private final Map<Class<?>, Object> instances;

    private Injector() {
        instances = new HashMap<>();
        interfaceImplementations = new HashMap<>();
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        if (clazz.isInterface()) {
            clazz = interfaceImplementations.get(clazz);
            if (clazz == null) {
                throw new RuntimeException("Implementation not found for interface: "
                        + clazz.getName());
            }
        }
        return createNewInstance(clazz);
    }

    private Object createNewInstance(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed, missing @Component before the class: "
                    + clazz.getName());
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            initializeFields(instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance/object of class: "
                    + clazz.getName(), e);
        }
    }

    private void initializeFields(Object instance) {
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                Object dependency = getInstance(fieldType);
                try {
                    field.set(instance, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class: "
                            + instance.getClass().getName() + " Field: " + field.getName(), e);
                }
            }
        }
    }
}
