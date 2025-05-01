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

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);

        if (clazz == null) {
            throw new RuntimeException("Class " + clazz.getName()
                    + " is null ");
        }
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz.getName()
                    + " must be annotated with @Component.");
        }

        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        Object clazzImplInstance = createNewInstance(clazz);

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                try {
                    Object fieldInstance = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(clazzImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error injecting dependency into field: "
                            + field.getName(), e);
                }
            }
        }
        instances.put(clazz, clazzImplInstance);
        return clazzImplInstance;
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
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                 | IllegalAccessException e) {
            throw new RuntimeException("Cannot create instance of " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
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
