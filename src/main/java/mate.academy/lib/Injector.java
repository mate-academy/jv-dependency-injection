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
    private static final Map<Class<?>, Class<?>> implementationsMap = new HashMap<>();
    private static final Injector injector = new Injector();
    private Map<Class<?>, Object> instancesMap = new HashMap<>();

    static {
        implementationsMap.put(ProductParser.class, ProductParserImpl.class);
        implementationsMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementationsMap.put(ProductService.class, ProductServiceImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> parseClazz) {
        Class<?> clazz = findImplementation(parseClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class have not annotation @Component "
                    + parseClazz.getName());
        }
        Object clazzImplInstance = createNewInstance(clazz);
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(clazzImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t initialisation tis field"
                            + field.getName());
                }
            }
        }
        return clazzImplInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instancesMap.containsKey(clazz)) {
            return instancesMap.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instancesMap.put(clazz, instance);
            return instance;
        } catch (InstantiationException
                | IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e) {
            throw new RuntimeException("Ca`nt create new instance " + e.getMessage());
        }
    }

    private Class<?> findImplementation(Class<?> clazz) {
        if (clazz.isInterface()) {
            return implementationsMap.get(clazz);
        }
        return clazz;
    }
}
