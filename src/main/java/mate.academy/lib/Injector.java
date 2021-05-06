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
    private Map<Class<?>, Object> createdObjects = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> inputClass) {
        Object clazzImplementation = null;
        Class<?> clazz = findImplementation(inputClass);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementation = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplementation, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t initialize class correctly"
                            + clazzImplementation.getClass());
                }
            }
        }
        if (clazzImplementation == null) {
            clazzImplementation = createNewInstance(clazz);
        }
        return clazzImplementation;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (createdObjects.containsKey(clazz)) {
            return createdObjects.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            createdObjects.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException | InstantiationException
                | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Can`t create instance of " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> inputClass) {
        Map<Class<?>, Class<?>> interfacesImplementations = new HashMap<>();
        interfacesImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfacesImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfacesImplementations.put(ProductService.class, ProductServiceImpl.class);
        if (inputClass.isInterface()) {
            Class<?> interfaceImplementation = interfacesImplementations.get(inputClass);
            if (!interfaceImplementation.isAnnotationPresent(Component.class)) {
                throw new RuntimeException("Can`t find correct implementation for interface,"
                        + "check that annotation @Component is present"
                        + interfaceImplementation.getName());
            }
            return interfaceImplementation;
        }
        return inputClass;
    }
}
