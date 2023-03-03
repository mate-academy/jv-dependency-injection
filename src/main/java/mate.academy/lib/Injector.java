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
    private static Map<Class<?>, Class<?>> implementations;
    private static Map<Class<?>, Object> instances;
    private static Injector injector;
    private static final String ANNOTATION = "Component";

    public Injector() {
        implementations = Map.of(
                FileReaderService.class, FileReaderServiceImpl.class,
                ProductParser.class, ProductParserImpl.class,
                ProductService.class, ProductServiceImpl.class);
        instances = new HashMap<>();
    }

    public static Injector getInjector() {
        if (injector == null) {
            injector = new Injector();
        }
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> implementationClazz = findImplementation(interfaceClazz);
        Field[] declaredFields = implementationClazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createInstance(implementationClazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't inject field "
                                    + field.getName() + " in class "
                                    + implementationClazz.getName() + ".", e);
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createInstance(implementationClazz);
        }
        return clazzImplementationInstance;
    }

    private Object createInstance(Class<?> implementationClazz) {
        if (instances.containsKey(implementationClazz)) {
            return instances.get(implementationClazz);
        }
        try {
            Constructor<?> constructor = implementationClazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(implementationClazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance for: "
                    + implementationClazz.getName() + ".", e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (!implementations.containsKey(interfaceClazz)) {
            throw new RuntimeException("Can't find interface implementation for "
                            + interfaceClazz.getName() + ".");
        }
        if (interfaceClazz.isInterface()) {
            Class<?> implementationClazz = implementations.get(interfaceClazz);
            if (!implementationClazz.isAnnotationPresent(Component.class)) {
                throw new RuntimeException("Can't find annotation "
                                + ANNOTATION + " for class "
                                + implementationClazz.getName() + ".");
            }
            return implementationClazz;
        }
        return interfaceClazz;
    }
}
