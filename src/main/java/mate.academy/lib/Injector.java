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
    private final Map<Class<?>, Class<?>> implementations = new HashMap<>();

    {
        implementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementations.put(ProductService.class, ProductServiceImpl.class);
        implementations.put(ProductParser.class, ProductParserImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object outputInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Object of the class "
                    + interfaceClazz.getName() + " couldn't be created");
        }
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field: declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = injector.getInstance(field.getType());
                outputInstance = createInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(outputInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't inject object to the field: "
                            + field.getName() + " in the class: " + clazz.getName());
                }
            }
        }
        if (outputInstance == null) {
            outputInstance = createInstance(clazz);
        }
        return outputInstance;
    }

    private Object createInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Couldn't create an instance of class: " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> clazz) {
        if (implementations.containsKey(clazz)) {
            return implementations.get(clazz);
        }
        if (clazz.isInterface()) {
            throw new RuntimeException("unknown class: " + clazz + " for injector");
        }
        return clazz;
    }
}
