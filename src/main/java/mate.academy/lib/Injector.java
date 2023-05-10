package mate.academy.lib;

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
    private static final Map<Class<?>, Class<?>> classImplementationMap = new HashMap<>();
    private static final Map<Class<?>, Object> instanceMap = new HashMap<>();

    static {
        classImplementationMap.put(ProductService.class, ProductServiceImpl.class);
        classImplementationMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        classImplementationMap.put(ProductParser.class, ProductParserImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementationClazz = findImplementation(interfaceClazz);
        if (!implementationClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can not create " + implementationClazz.getName()
                    + "class with Injector. Please consider using @Component");
        }

        if (instanceMap.containsKey(implementationClazz)) {
            return instanceMap.get(implementationClazz);
        }

        try {
            Object newInstance = implementationClazz.getConstructor().newInstance();
            Field[] fields = implementationClazz.getDeclaredFields();
            for (Field field: fields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(newInstance, fieldInstance);
                }
            }

            return newInstance;
        } catch (NoSuchMethodException | InvocationTargetException
                    | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Can not create: "
                    + implementationClazz.getName() + " class.", e);
        }
    }

    private Class<?> findImplementation(Class<?> clazz) {
        if (clazz.isInterface()) {
            return Injector.classImplementationMap.get(clazz);
        }
        return clazz;
    }
}
