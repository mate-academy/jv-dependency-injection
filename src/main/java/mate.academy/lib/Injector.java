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
    private final Map<Class<?>, Object> newInstances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationsInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        isAnnotationComponentPresent(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationsInstance = createNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationsInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. "
                            + "Class: " + clazz.getName() + ". Field: " + field.getName());
                }
            }
        }
        if (clazzImplementationsInstance == null) {
            clazzImplementationsInstance = createNewInstance(clazz);
        }
        return clazzImplementationsInstance;
    }

    private void isAnnotationComponentPresent(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("This Class " + clazz.getName() + " can't be "
                    + "initialized with injector, it hasn't annotation @Component.");
        }
    }

    private Object createNewInstance(Class<?> clazz) {
        if (newInstances.containsKey(clazz)) {
            return newInstances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            newInstances.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                 | IllegalAccessException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> implementations = new HashMap<>();
        implementations.put(ProductParser.class, ProductParserImpl.class);
        implementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementations.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return implementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
