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
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> type) {
        Class<?> implementationClass = findImplementation(type);

        // Checking if there is any annotations "Component"
        if (!implementationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed: "
                    + implementationClass.getName() + " is not a @Component");
        }

        Object newObject = createNewInstance(implementationClass);

        Field[] declaredFields = implementationClass.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                // рекурсия
                Object dependencyObject = getInstance(field.getType());
                // вставляем
                try {
                    field.setAccessible(true);
                    field.set(newObject, dependencyObject);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException("Cant initialize field value. Class:"
                            + implementationClass.getName() + ".Field:"
                            + field.getName());
                }
            }
        }

        if (newObject == null) {
            newObject = createNewInstance(implementationClass);
        }

        return newObject;
    }

    private Object createNewInstance(Class<?> clazz) {
        // if we create an object lets use it
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        // create new object
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException | SecurityException
                | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException("Can't create a new instance of" + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
        interfaceImplementations.put(ProductService.class,
                ProductServiceImpl.class);
        interfaceImplementations.put(ProductParser.class,
                ProductParserImpl.class);
        interfaceImplementations.put(FileReaderService.class,
                FileReaderServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }

}
