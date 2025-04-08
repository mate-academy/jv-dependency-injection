package mate.academy.lib;

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
    private static final Map<Class<?>, Object> instances = new HashMap<>();
    private static final Map<Class<?>, Class<?>> interfaceToImpl = Map.of(
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class
    );

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }

        try {
            Class<?> implClass = interfaceClazz;
            if (interfaceClazz.isInterface()) {
                implClass = interfaceToImpl.get(interfaceClazz);
                if (implClass == null) {
                    throw new RuntimeException("No implementation found for "
                            + interfaceClazz.getName());
                }
            }

            Object instance = createInstance(implClass);
            instances.put(interfaceClazz, instance);
            return instance;
        } catch (NoSuchMethodException
                 | InstantiationException
                 | IllegalAccessException
                 | InvocationTargetException e) {
            throw new RuntimeException("Error creating instance of "
                    + interfaceClazz.getName(), e);
        }
    }

    public Object createInstance(Class<?> clazz) throws NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException,
            InstantiationException {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new IllegalArgumentException("Class"
                    + clazz.getName() + " is not a component.");
        }

        Object instance = clazz.getDeclaredConstructor().newInstance();
        injectDependencies(instance);
        return instance;
    }

    public void injectDependencies(Object instance) throws IllegalAccessException {
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);

                Class<?> fieldType = field.getType();
                Object dependency = getInstance(fieldType);
                field.set(instance,dependency);
            }
        }
    }
}
