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
    private static final Injector INJECTOR = new Injector();

    private static final Map<Class<?>, Class<?>> INTERFACE_IMPLEMENTATIONS = Map.of(
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class
    );

    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return INJECTOR;
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> interfaceClazz) {
        Class<?> clazz = findImpl(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "The class " + clazz.getSimpleName() + " is not annotated with @Component"
            );
        }
        if (instances.containsKey(clazz)) {
            return (T) instances.get(clazz);
        }
        Object clazzImplInstance = createNewInstance(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(clazzImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot initialize field value. Class: "
                            + clazz.getName()
                            + ". Field: " + field.getName(), e);
                }
            }
        }
        return (T) clazzImplInstance;
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
            throw new RuntimeException("Cannot create the new instance of "
                    + clazz.getName(), e);
        }
    }

    private Class<?> findImpl(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            Class<?> impl = INTERFACE_IMPLEMENTATIONS.get(interfaceClazz);
            if (impl == null) {
                throw new RuntimeException("No implementation found for: "
                        + interfaceClazz.getName());
            }
            return impl;
        }
        return interfaceClazz;
    }
}
