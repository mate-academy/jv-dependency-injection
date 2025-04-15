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

    private final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implClass = findImplementation(interfaceClazz);

        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + implClass.getName()
                    +
                    " is not annotated with @Component");
        }

        if (instances.containsKey(implClass)) {
            return instances.get(implClass);
        }

        Object instance = createNewInstance(implClass);

        for (Field field : implClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't inject field " + field.getName(), e);
                }
            }
        }

        instances.put(implClass, instance);
        return instance;
    }

    private Object createNewInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create an instance of: " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        return interfaceClazz.isInterface()
                ? interfaceImplementations.get(interfaceClazz)
                : interfaceClazz;
    }
}