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
    private static final Class<Component> CLASS_ANNOTATION = Component.class;
    private static final Class<Inject> FIELD_ANNOTATION = Inject.class;
    private static final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object instance = null;
        Class<?> clazzImplementation = findImplementation(interfaceClazz);
        if (!clazzImplementation.isAnnotationPresent(CLASS_ANNOTATION)) {
            throw new RuntimeException("Can't create an instance for the unsupported Class: "
            + clazzImplementation.getName());
        }
        Field[] fields = clazzImplementation.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(FIELD_ANNOTATION)) {
                Object fieldInstance = getInstance(field.getType());
                instance = createNewInstance(clazzImplementation);
                try {
                    field.setAccessible(true);
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field. Class: "
                            + clazzImplementation.getName() + " Field: " + field.getName());
                }
            }
        }
        return instance == null ? createNewInstance(clazzImplementation) : instance;
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
        } catch (NoSuchMethodException | IllegalAccessException
                 | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException("Can't create an instance for the Class: "
                    + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            return interfaceClazz;
        }
        if (!interfaceImplementations.containsKey(interfaceClazz)) {
            throw new RuntimeException("Can't find an implementation for the Interface: "
                    + interfaceClazz.getName());
        }
        return interfaceImplementations.get(interfaceClazz);
    }
}
