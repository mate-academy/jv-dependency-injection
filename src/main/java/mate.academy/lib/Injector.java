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
    private final Map<Class<?>, Class<?>> implementations = Map.of(FileReaderService.class,
            FileReaderServiceImpl.class, ProductParser.class,
            ProductParserImpl.class, ProductService.class,
            ProductServiceImpl.class);
    private final Map<Class<?>, Object> components = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementation = findImplementation(interfaceClazz);
        Object clazzImplementationsInstance = instanceOf(implementation);
        Field[] fields = implementation.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                try {
                    Object value = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(clazzImplementationsInstance, value);
                } catch (SecurityException | IllegalAccessException e) {
                    throw new RuntimeException("Can't set value of field. "
                            + "Class: " + implementation.getName()
                            + ". Field: " + field.getName());
                }
            }
        }
        return clazzImplementationsInstance;
    }

    private Object instanceOf(Class<?> implementation) {
        boolean contains = components.containsKey(implementation);
        if (contains) {
            return components.get(implementation);
        }
        try {
            Constructor<?> constructor = implementation.getConstructor();
            Object instance = constructor.newInstance();
            components.put(implementation, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of " + implementation.getName());
        }
    }

    private Class<?> findImplementation(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)
                && !implementations.containsKey(clazz)) {
            throw new RuntimeException("Unsupported class " + clazz.getName() + "!"
                    + "Didn't have supported implementation or @Component annotation!");
        }
        Class<?> impl = implementations.get(clazz);
        if (impl != null) {
            return impl;
        } else {
            return clazz;
        }
    }
}
