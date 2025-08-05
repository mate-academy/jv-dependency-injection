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
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Field[] fields = interfaceClazz.getDeclaredFields();
        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzImplementationInstance = null;

        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                if (!field.getType().isAnnotationPresent(Component.class)) {
                    throw new RuntimeException("@Component annotation is not present in class "
                            + field.getType().getName());
                }

                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);

        if (interfaceClazz.isInterface()) {
            Class<?> impl = interfaceImplementations.get(interfaceClazz);
            if (impl == null) {
                throw new RuntimeException("No implementation found for interface: "
                        + interfaceClazz.getName());
            }
            return impl;
        }

        if (!interfaceClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Unsupported class: " + interfaceClazz.getName());
        }

        return interfaceClazz;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        Constructor<?> constructor = null;
        try {
            constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException | InvocationTargetException
                 | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Cannot create instance of " + clazz.getName(), e);
        }

    }
}
