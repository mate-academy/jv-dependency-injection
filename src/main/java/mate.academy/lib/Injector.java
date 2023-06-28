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
    private static final Map<Class<?>, Object> INSTANCES = new HashMap<>();
    private static final Map<Class<?>, Class<?>> IMPLEMENTATIONS = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    private static final Injector injector = new Injector();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object implementationInstance = null;
        Class<?> implementation = findImplementation(interfaceClazz);
        for (Field field : implementation.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                implementationInstance = createNewInstance(implementation);
                try {
                    field.setAccessible(true);
                    field.set(implementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(String.format(
                            "Cannot initialize field value '%s' of class '%s'",
                            field.getName(), implementation.getName()
                    ), e);
                }
            }
        }
        // in case if provided class has no fields
        if (implementationInstance == null) {
            implementationInstance = createNewInstance(implementation);
        }
        return implementationInstance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Class<?> implementation = interfaceClazz.isInterface()
                ? IMPLEMENTATIONS.get(interfaceClazz)
                : interfaceClazz;

        if (!implementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(String.format(
                    "Cannot find implementation for '%s' interface. "
                            + "Check if @Component annotation is present.",
                    interfaceClazz.getName())
            );
        }
        return implementation;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (INSTANCES.containsKey(clazz)) {
            return INSTANCES.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            INSTANCES.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cannot create a new instance of " + clazz.getName(), e);
        }
    }
}
