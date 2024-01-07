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

    private final Map<Class<?>,Class<?>> mapOfInterfaceImplementation =
            Map.of(FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplInstance = null;
        Class<?> clazz = findObjectInTheMap(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed, "
                    + "missing @Component annotaion on the class " + clazz.getName());
        }
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplInstance = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error " + e);
                }
            }
        }

        if (clazzImplInstance == null) {
            return createNewInstance(clazz);
        }
        return clazzImplInstance;
    }

    public Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz,instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("A new instance can not be created " + e);
        }
    }

    public Class<?> findObjectInTheMap(Class<?> clazz) {
        if (mapOfInterfaceImplementation.containsKey(clazz)) {
            return mapOfInterfaceImplementation.get(clazz);
        }
        return clazz;
    }
}
