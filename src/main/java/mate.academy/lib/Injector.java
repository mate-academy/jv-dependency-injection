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

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object instance = instances.get(interfaceClazz);
        if (instance == null) {
            Class<?> implClass = findImplementation(interfaceClazz);
            instance = createInstance(implClass);
            instances.put(interfaceClazz, instance);
        }
        return instance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.equals(ProductService.class)) {
            return ProductServiceImpl.class;
        } else if (interfaceClazz.equals(ProductParser.class)) {
            return ProductParserImpl.class;
        } else if (interfaceClazz.equals(FileReaderService.class)) {
            return FileReaderServiceImpl.class;
        }
        throw new RuntimeException("No implementation found for " + interfaceClazz.getName());
    }

    private Object createInstance(Class<?> implClass) {
        Object instance = instances.get(implClass);
        if (instance != null) {
            return instance;
        }

        try {
            instance = implClass.getDeclaredConstructor().newInstance();
            instances.put(implClass, instance);

            for (Field field : implClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    Object fieldInstance = getInstance(field.getType());
                    field.set(instance, fieldInstance);
                }
            }

            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create instance of " + implClass.getName(), e);
        }
    }
}
