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
    private static final Map<Class<?>, Class<?>> interfaceImplementation = Map.of(
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class
    );
    private static final String COMPONENT_ANNOTATION_ERROR
            = "Class: %s isn't declared as @Component";
    private static final String FIELD_INITIALIZE_ERROR
            = "Can not initialize field value. Class: %s. Field: %s";
    private static final String INSTANCE_CREATE_ERROR
            = "Can't create instance from class: %s";
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        checkClassAnnotation(clazz);
        clazzImplementationInstance = getOrCreateNewInstance(clazz);
        injectFields(clazz, clazzImplementationInstance);
        return clazzImplementationInstance;
    }

    private void injectFields(Class<?> clazz, Object clazzImplementationInstance) {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(String
                            .format(FIELD_INITIALIZE_ERROR, clazz.getName(), field.getName()), e);
                }
            }
        }
    }

    private static void checkClassAnnotation(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(String.format(COMPONENT_ANNOTATION_ERROR, clazz.getName()));
        }
    }

    private Object getOrCreateNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(String.format(INSTANCE_CREATE_ERROR, clazz.getName()), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        return interfaceClazz.isInterface()
                ? interfaceImplementation.get(interfaceClazz)
                : interfaceClazz;
    }
}
