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
        Object clazzImplInstance = null;
        Class<?> clazz = findImpl(interfaceClazz);

        if (clazz.isAnnotationPresent(Component.class)) {
            Field[] declaredFields = clazz.getDeclaredFields();

            for (Field field: declaredFields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    clazzImplInstance = createNewInstance(clazz);

                    try {
                        field.setAccessible(true);
                        field.set(clazzImplInstance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Cant initialise field: " + field.getName()
                                + " In class: " + clazz.getName());
                    }
                }
            }

            if (clazzImplInstance == null) {
                clazzImplInstance = createNewInstance(clazz);
            }
            return clazzImplInstance;
        } else {
            throw new RuntimeException("Class " + clazz.getName()
                    + "Don't have @Component annotation");
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
        } catch (NoSuchMethodException | InstantiationException
                  | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Cant create new instance: " + clazz.getName());
        }
    }

    private Class<?> findImpl(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImpl = new HashMap<>();

        interfaceImpl.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImpl.put(ProductParser.class, ProductParserImpl.class);
        interfaceImpl.put(ProductService.class, ProductServiceImpl.class);

        if (interfaceClazz.isInterface()) {
            return interfaceImpl.get(interfaceClazz);
        }

        return interfaceClazz;
    }
}
