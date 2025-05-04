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
    private static final Map<Class<?>, Class<?>> interfaceImplementations;
    private Map<Class<?>, Object> instances = new HashMap<>();

    static {
        interfaceImplementations = Map.of(
                FileReaderService.class, FileReaderServiceImpl.class,
                ProductParser.class, ProductParserImpl.class,
                ProductService.class, ProductServiceImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] fields = interfaceClazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class:"
                            + clazz.getName() + ". Field: " + field.getName());
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
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
        } catch (NoSuchMethodException | InvocationTargetException
                 | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceImplementations.containsValue(interfaceClazz)) {
            return interfaceClazz;
        }
        Class<?> implementationClass = interfaceImplementations.get(interfaceClazz);
        if (implementationClass == null) {
            throw new RuntimeException("Can't find implementation for " + interfaceClazz.getName());
        }
        if (!implementationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed, missing @Component annotation "
                    + "on the class " + implementationClass.getName());
        }
        return implementationClass;
    }
}
