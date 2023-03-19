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
    private static final Map<Class<?>, Class<?>> implementations = new HashMap<>();
    private static final Map<Class<?>, Object> components = new HashMap<>();
    private static final Injector injector = new Injector();

    private Injector() {
        fillMap();
    }

    private static void fillMap() {
        implementations.put(FileReaderService.class,
                FileReaderServiceImpl.class);
        implementations.put(ProductParser.class,
                ProductParserImpl.class);
        implementations.put(ProductService.class,
                ProductServiceImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementation = getImpl(interfaceClazz);
        Object clazzImplementationsInstance = instanceOf(implementation);
        Field[] fields = implementation.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object value = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationsInstance, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't set value of field. "
                            + "Class: " + implementation.getName()
                            + ". Field: " + field.getName());
                }
            }
        }
        return clazzImplementationsInstance;
    }

    private Object instanceOf(Class<?> implementation) {
        boolean present = implementation.isAnnotationPresent(Component.class);
        boolean contains = components.containsKey(implementation);
        if (present && contains) {
            return components.get(implementation);
        } else {
            try {
                Constructor<?> constructor = implementation.getConstructor();
                Object instance = constructor.newInstance();
                if (present) {
                    components.put(implementation, instance);
                }
                return instance;
            } catch (NoSuchMethodException | InvocationTargetException
                     | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Can't create instance of " + implementation.getName());
            }
        }
    }

    private Class<?> getImpl(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)
                && !implementations.containsKey(clazz)) {
            throw new RuntimeException("Unsupported class " + clazz.getName() + "!");
        }
        Class<?> impl = implementations.get(clazz);
        if (impl != null) {
            return impl;
        } else {
            return clazz;
        }
    }
}
