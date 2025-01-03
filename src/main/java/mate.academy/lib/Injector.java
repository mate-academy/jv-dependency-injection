package mate.academy.lib;

import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplInstance = null;
        Class<?> clazz = findImpl(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        if (clazz.isAnnotationPresent(Component.class)) {
            for (Field field: declaredFields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    clazzImplInstance = createNewInstance(clazz);
                    try {
                        field.setAccessible(true);
                        field.set(clazzImplInstance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Can't initialize field value");
                    }
                }
            }
        } else {
            throw new RuntimeException("Invalid class");
        }
        if (clazzImplInstance == null) {
            clazzImplInstance = createNewInstance(clazz);
        }
        return clazzImplInstance;
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
            throw new RuntimeException("Can't create instance of class " + clazz);
        }
    }

    private Class<?> findImpl(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> implementations = new HashMap<>();
        implementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementations.put(ProductParser.class, ProductParserImpl.class);
        implementations.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return implementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
