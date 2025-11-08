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
    private static final Map<Class<?>, Class<?>> IMPLEMENTATIONS = Map.of(
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class
    );

    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        return createNewInstance(clazz);
    }

    private Class<?> findImplementation(Class<?> clazz) {
        if (clazz.isInterface()) {
            Class<?> impl = IMPLEMENTATIONS.get(clazz);
            if (impl == null) {
                throw new RuntimeException("No implementation found for interface: "
                        + clazz.getName());
            }
            return impl;
        }

        if (!IMPLEMENTATIONS.containsValue(clazz)
                && !clazz.equals(FileReaderServiceImpl.class)
                && !clazz.equals(ProductParserImpl.class)
                && !clazz.equals(ProductServiceImpl.class)) {
            throw new RuntimeException("Unsupported class: " + clazz.getName());
        }

        return clazz;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz.getName()
                    + " is not annotated with @Component");
        }

        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        Object object;
        try {
            Constructor<?> constructor = clazz.getConstructor();
            object = constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of: " + clazz.getName(), e);
        }

        instances.put(clazz, object);

        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                if (field.getType().isPrimitive()) {
                    continue;
                }
                Object dependency = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(object, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't inject field: " + field.getName()
                            + " in class: " + clazz.getName(), e);
                }
            }
        }
        return object;
    }
}
