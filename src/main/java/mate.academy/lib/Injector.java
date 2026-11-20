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
    private final Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    private Injector() {
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz.getName()
                    + " must be annotated with @Component");
        }
        Object clazzImplementationInstance = createNewInstance(clazz);
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't set field " + field.getName()
                            + " in class " + clazz.getName(), e);
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
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
            Class<?> implementation = interfaceImplementations.get(interfaceClazz);
            if (implementation == null) {
                throw new RuntimeException("No implementation found for interface: "
                        + interfaceClazz.getName());
            }
            return implementation;
        }
        if (!interfaceImplementations.containsValue(interfaceClazz)) {
            throw new RuntimeException("Unsupported class: " + interfaceClazz.getName());
        }
        return interfaceClazz;
    }
}
