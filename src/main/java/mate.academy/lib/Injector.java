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

    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object interfaceClazzInstance = null;
            Class<?> clazz = findImplementation(interfaceClazz);
        if (clazz.isAnnotationPresent(Component.class)) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());

                    interfaceClazzInstance = createNewInstance(clazz);

                    try {
                        field.setAccessible(true);
                        field.set(interfaceClazzInstance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Can't initialize field value."
                                + " Class: " + clazz.getName() + ". Field: " + field.getName());
                    }

                }
            }
            if (interfaceClazzInstance == null) {
                interfaceClazzInstance = createNewInstance(clazz);
            }
            return interfaceClazzInstance;
        } else {
            throw new RuntimeException("Class should be marked by Component annotation");
        }

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
        } catch (NoSuchMethodException | InvocationTargetException
                 | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImpl = new HashMap<>();
        interfaceImpl.put(ProductParser.class, ProductParserImpl.class);
        interfaceImpl.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImpl.put(ProductService.class, ProductServiceImpl.class);

        if (interfaceClazz.isInterface()) {
            return interfaceImpl.get(interfaceClazz);
        } else {
            return interfaceClazz;
        }
    }
}
