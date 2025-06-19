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
    private static Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public static Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz.getName()
                    + " is not annotated with @Component");
        }
        Object clazzImplementationInstance = createNewInstance(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value, Clazz: "
                            + clazz.getName() + " Field: " + field.getName());
                }
            }
        }
        return clazzImplementationInstance;
    }

    public static Object getInstance(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return getInstance(clazz);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't find class: " + className, e);
        }
    }

    private static Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        Constructor<?> constructor = null;
        try {
            constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (InstantiationException | NoSuchMethodException
                     | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Can't create a new instance of: " + clazz.getName());
        }
    }

    private static Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceToImpl = new HashMap<>();
        interfaceToImpl.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceToImpl.put(ProductParser.class, ProductParserImpl.class);
        interfaceToImpl.put(ProductService.class, ProductServiceImpl.class);
        Class<?> implClass = interfaceToImpl.get(interfaceClazz);
        if (implClass == null) {
            throw new RuntimeException("No implementation found for " + interfaceClazz.getName());
        }
        if (interfaceClazz.isInterface()) {
            return interfaceToImpl.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
