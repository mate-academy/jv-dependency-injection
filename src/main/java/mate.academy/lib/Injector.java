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
    private static final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object implementationInstance = null;
        Class<?> implementationClazz = findImplementation(interfaceClazz);
        checkClassAnnotation(implementationClazz);
        Field[] fields = interfaceClazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                implementationInstance = createNewInstance(implementationClazz);
                field.setAccessible(true);
                try {
                    field.set(implementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't set " + field.getName()
                            + " to " + implementationClazz.getName(), e);
                }
            }
        }
        return implementationInstance == null
                ? createNewInstance(implementationClazz)
                : implementationInstance;
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
            throw new RuntimeException("Can't create instance from class: " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        return interfaceClazz.isInterface()
                ? interfaceImplementations.get(interfaceClazz)
                : interfaceClazz;
    }

    private void checkClassAnnotation(Class<?> implementationClazz) {
        if (!implementationClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class: " + implementationClazz.getName()
                    + " isn't declared as @Component");
        }
    }
}
