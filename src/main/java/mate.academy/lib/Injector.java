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
    private final Map<Class<?>, Class<?>> interfaceImplementation = new HashMap<>();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    private Injector() {
        interfaceImplementation.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementation.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementation.put(ProductService.class, ProductServiceImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> type) {
        try {
            Class<?> clazz = findImplementation(type);
            checkOfComponent(clazz);
            if (clazz.isInterface()) {
                return instances.get(clazz);
            }
            Object instance = createNewInstance(clazz);
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    field.setAccessible(true);
                    try {
                        field.set(instance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of " + type.getName(), e);
        }
    }

    private Object createNewInstance(Class<?> clazz) throws ReflectiveOperationException {
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        return constructor.newInstance();
    }

    private Class<?> findImplementation(Class<?> type) {
        if (type.isInterface()) {
            Class<?> impl = interfaceImplementation.get(type);
            if (impl == null) {
                throw new RuntimeException("No implementation found for " + type.getName());
            }
            return impl;
        }
        return type;
    }

    private void checkOfComponent(Class<?> clazz) throws ReflectiveOperationException {
        if (!clazz.isAnnotationPresent(Component.class)) {
            try {
                throw new ReflectiveOperationException("Class " + clazz.getName()
                        + " is not marked with annotation @Component");
            } catch (ReflectiveOperationException e) {
                throw new ReflectiveOperationException(e);
            }
        }
    }
}
