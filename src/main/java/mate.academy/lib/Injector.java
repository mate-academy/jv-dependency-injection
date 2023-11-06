package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final String ANNOTATION_CHECK_FAILED_MESSAGE = "Class %s "
            + "is not annotated as @Component!";
    private static final String FIELD_SET_FAILED_MESSAGE = "Problem with"
            + " field set. Class: %s. Field %s!";
    private static final String INSTANCE_CREATE_FAILED_MESSAGE = "Unreached "
            + "creating new instance of: %s!";
    private static final Injector injector = new Injector();

    private final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(String
                    .format(ANNOTATION_CHECK_FAILED_MESSAGE, clazz.getName()));
        }
        Object clazzImplInstance = null;
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplInstance = createNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplInstance, fieldInstance);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(String
                            .format(FIELD_SET_FAILED_MESSAGE,
                                    clazz.getName(), field.getName()), ex);
                }
            }
        }
        return clazzImplInstance == null
                ? createNewInstance(clazz)
                : clazzImplInstance;
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
        } catch (NoSuchElementException | InvocationTargetException | NoSuchMethodException
                 | InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(String
                    .format(INSTANCE_CREATE_FAILED_MESSAGE, clazz.getName()), ex);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isAnnotationPresent(Component.class)) {
            return interfaceClazz;
        }
        if (interfaceClazz.isInterface()) {
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
