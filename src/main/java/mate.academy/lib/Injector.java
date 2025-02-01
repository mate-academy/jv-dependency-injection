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

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        try {
            Class<?> implClass = findImplementation(interfaceClazz);
            if (!implClass.isAnnotationPresent(Component.class)) {
                throw new RuntimeException("Class " + interfaceClazz.getName()
                        + " is not annotated with @Component");
            }

            if (instances.containsKey(interfaceClazz)) {
                return instances.get(interfaceClazz);
            }

            Object classImplementationInstance = createNewInstance(interfaceClazz);
            instances.put(interfaceClazz, classImplementationInstance);
            return classImplementationInstance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create an instance of "
                    + interfaceClazz.getName(), e);
        }
    }

    private static Class<?> findImplementation(Class<?> interfaceClass) {
        Map<Class<?>, Class<?>> interfaceToImplMap = Map.of(FileReaderService.class,
                FileReaderServiceImpl.class,
                ProductParser.class, ProductParserImpl.class,
                ProductService.class, ProductServiceImpl.class
        );
        return interfaceToImplMap.getOrDefault(interfaceClass, interfaceClass);
    }

    private Object createNewInstance(Class<?> interfaceClazz) throws IllegalAccessException {
        Class<?> implClass = findImplementation(interfaceClazz);
        Constructor<?> constructor;
        try {
            constructor = implClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No default constructor found for "
                    + implClass.getName(), e);
        }
        Object instance;
        try {
            instance = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException
                 | InvocationTargetException e) {
            throw new RuntimeException("Failed to create an instance of "
                    + implClass.getName(), e);
        }

        for (Field field : implClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Class<?> fieldType = field.getType();
                Object dependency = getInstance(fieldType);
                field.setAccessible(true);
                field.set(instance, dependency);
            }
        }

        instances.put(interfaceClazz, instance);
        return instance;
    }
}


