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

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        hasComponentAnnotation(clazz);
        Field[] declaredFiends = clazz.getDeclaredFields();
        for (Field field : declaredFiends) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzInstance = createNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cant initialize field value. Class: "
                            + clazz.getName() + ". Field: " + field.getName());
                }
            }
        }
        if (clazzInstance == null) {
            clazzInstance = createNewInstance(clazz);
        }
        return clazzInstance;
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
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                 | IllegalAccessException e) {
            throw new RuntimeException("Cant create a new  instance of " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> implementationInterface = new HashMap<>();
        implementationInterface.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementationInterface.put(ProductParser.class, ProductParserImpl.class);
        implementationInterface.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return implementationInterface.get(interfaceClazz);
        }
        return interfaceClazz;
    }

    private void hasComponentAnnotation(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injector not completed. Annotation is not present "
                    + "on the class " + clazz.getName());
        }
    }
}
