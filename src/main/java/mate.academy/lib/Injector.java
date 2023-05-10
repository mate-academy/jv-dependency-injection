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
    private final Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
    private final Map<Class<?>, Object> classesInstances = new HashMap<>();

    {
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementationClazz = findImplementation(interfaceClazz);
        if (!implementationClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(implementationClazz + " doesn't support injection");
        }
        Object newInstance = getNewInstance(implementationClazz);
        Field[] declaredFields = implementationClazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(declaredField.getType());
                declaredField.setAccessible(true);
                try {
                    declaredField.set(newInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field " + declaredField.getName()
                            + " in the class " + interfaceClazz.getName(), e);
                }
            }
        }
        return newInstance;
    }

    private Object getNewInstance(Class<?> clazz) {
        if (classesInstances.containsKey(clazz)) {
            return classesInstances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object newInstance = constructor.newInstance();
            classesInstances.put(clazz, newInstance);
            return newInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create new instance for " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceImplementations.containsKey(interfaceClazz)) {
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
