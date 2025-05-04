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
    private static final Class<Component> CLASSES_ANNOTATION = Component.class;
    private static final Class<Inject> FIELDS_ANNOTATION = Inject.class;
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class
    );
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = interfaceClazz;
        if (interfaceClazz.isInterface()) {
            clazz = interfaceImplementations.get(interfaceClazz);
        }
        if (!clazz.isAnnotationPresent(CLASSES_ANNOTATION)) {
            throw new RuntimeException("Injection failed, missing @Component "
                    + " annotation on the class " + clazz);
        }

        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(FIELDS_ANNOTATION)) {
                Object fieldInstance = getInstance(declaredField.getType());
                clazzImplementationInstance = createNewInstance(clazz);
                try {
                    declaredField.setAccessible(true);
                    declaredField.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t initialize field. Class: "
                            + clazz.getName() + ". Field: " + declaredField.getName(), e);
                }
            }
        }
        return clazzImplementationInstance == null ? createNewInstance(clazz)
                : clazzImplementationInstance;
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
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can`t create instance of " + clazz.getName());
        }
    }
}
