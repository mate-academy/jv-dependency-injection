package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }
        Class<?> clazz = findImplementation(interfaceClazz);
        if (clazz == null || !clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("No implementation found for: " + interfaceClazz.getName());
        }
        Object instance = createInstance(clazz);
        instances.put(interfaceClazz, instance);
        return instance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            if (interfaceClazz.equals(FileReaderService.class)) {
                return mate.academy.service.impl.FileReaderServiceImpl.class;
            } else if (interfaceClazz.equals(ProductParser.class)) {
                return mate.academy.service.impl.ProductParserImpl.class;
            } else if (interfaceClazz.equals(ProductService.class)) {
                return mate.academy.service.impl.ProductServiceImpl.class;
            }
        }
        return interfaceClazz;
    }

    private Object createInstance(Class<?> clazz) {
        Constructor<?> constructor;
        try {
            constructor = clazz.getDeclaredConstructor();
            Object instance = constructor.newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    Object fieldInstance = getInstance(field.getType());
                    field.set(instance, fieldInstance);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of class: " + clazz.getName(), e);
        }
    }
}
