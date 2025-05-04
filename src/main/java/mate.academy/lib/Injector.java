package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
    private final Map<Class<?>, Class<?>> classesImplementation = new HashMap<>() {{
            put(ProductService.class, ProductServiceImpl.class);
            put(ProductParser.class, ProductParserImpl.class);
            put(FileReaderService.class, FileReaderServiceImpl.class);
        }};

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!instances.containsKey(clazz)) {
            try {
                Constructor<?> constructor = clazz.getConstructor();
                Object instance = constructor.newInstance();
                instances.put(clazz, instance);
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Inject.class)) {
                        Object fieldInstance = getInstance(field.getType());
                        field.setAccessible(true);
                        field.set(instance, fieldInstance);
                    }
                }
            } catch (NoSuchMethodException | IllegalAccessException
                     | InstantiationException
                     | InvocationTargetException e) {
                throw new RuntimeException("Failed to instantiate class: " + clazz.getName(), e);
            }
        }
        return instances.get(clazz);
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            Class<?> implementationClass = classesImplementation.get(interfaceClazz);
            if (implementationClass != null) {
                return implementationClass;
            }
        } else if (classesImplementation.containsValue(interfaceClazz)) {
            return interfaceClazz;
        }
        throw new RuntimeException("No implementation found for: "
                + interfaceClazz.getName());
    }
}
