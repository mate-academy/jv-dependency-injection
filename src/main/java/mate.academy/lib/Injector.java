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
    private static final Map<Class<?>, Class<?>> interfaceImpls = new HashMap<>();

    static {
        interfaceImpls.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImpls.put(ProductParser.class, ProductParserImpl.class);
        interfaceImpls.put(ProductService.class, ProductServiceImpl.class);
    }

    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImpl(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        Object clazzImplInstance = createNewInstance(clazz);
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(clazzImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error injecting field."
                            + "Class: " + clazz.getName()
                            + ". Field: " + field.getName(), e);
                }
            }
        }
        return clazzImplInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            if (!clazz.isAnnotationPresent(Component.class)) {
                throw new RuntimeException(
                        "Class is not annotated with @Component: " + clazz.getName());
            }
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Cannot create new instance of " + clazz.getName(), e);
        }
    }

    private Class<?> findImpl(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return interfaceImpls.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
