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
    private static Map<Class<?>, Object> instances = new HashMap<>();
    private static Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object instanceOfImplementation;
        Class<?> clazz = getImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz.getName()
                    + " doesn't mark with @Component annotation");
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field: fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                instanceOfImplementation = createInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(instanceOfImplementation, fieldInstance);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Can't set instance of "
                            + fieldInstance.getClass() + " to instance of "
                            + instanceOfImplementation.getClass(), e);
                }
            }
        }
        instanceOfImplementation = createInstance(clazz);
        return instanceOfImplementation;
    }

    private Object createInstance(Class<?> clazz) {
        if (!instances.containsKey(clazz)) {
            try {
                Constructor<?> clazzConstructor = clazz.getConstructor();
                Object newInstance = clazzConstructor.newInstance();
                instances.put(clazz, newInstance);
                return newInstance;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Can't create a new instance of class: " + clazz, e);
            }
        }
        return instances.get(clazz);
    }

    private Class getImplementation(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            return interfaceClazz;
        }
        if (interfaceImplementations.containsKey(interfaceClazz)) {
            return interfaceImplementations.get(interfaceClazz);
        }
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        return interfaceImplementations.get(interfaceClazz);
    }
}
