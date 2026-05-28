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
    private final Map<Class<?>, Class<?>> interfaceImplementation = Map.of(
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class
    );

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredField = clazz.getDeclaredFields();
        for (Field field : declaredField) {
            if (field.isAnnotationPresent(Inject.class)) {
                checkIsComponent(field.getType());
                Object fieldInstance = getInstance(field.getType());
                instances.put(fieldInstance.getClass(), fieldInstance);
                if (instances.containsKey(clazz)) {
                    clazzImplInstance = instances.get(clazz);
                } else {
                    clazzImplInstance = createInstance(clazz);
                }
                field.setAccessible(true);
                try {
                    field.set(clazzImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't crete class instance "
                            + clazz
                            + " "
                            + clazz.getName()
                            + " Field: " + field.getName(), e);
                }
            }
        }
        if (clazzImplInstance == null) {
            clazzImplInstance = createInstance(clazz);
        }
        return clazzImplInstance;
    }

    private void checkIsComponent(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class don't have required annotation " + clazz.getName());
        }
    }

    private Object createInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Object instance = clazz.getConstructor().newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create new class instance", e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return interfaceImplementation.get(interfaceClazz);
        }
        return interfaceImplementation.get(interfaceClazz);
    }
}
