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
    private final Map<Class<?>, Class<?>> implementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = getImplementationClass(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("There isn't present required annotation!");
        }
        Field[] fields = clazz.getDeclaredFields();
        Object clazzInstance = null;
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzInstance = createClazzInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field " + field.getName());
                }
            }
        }
        if (clazzInstance == null) {
            clazzInstance = createClazzInstance(clazz);
        }
        return clazzInstance;
    }

    private Object createClazzInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> clazzConstructor = clazz.getConstructor();
            Object clazzInstance = clazzConstructor.newInstance();
            instances.put(clazz, clazzInstance);
            return clazzInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create new instance of class " + clazz.getName());
        }
    }

    private Class<?> getImplementationClass(Class<?> interfaceClazz) {
        if (implementations.containsKey(interfaceClazz)) {
            return implementations.get(interfaceClazz);
        }
        if (interfaceClazz.isInterface()) {
            throw new RuntimeException("There isn't implementation present for "
                    + interfaceClazz
                    + ". A new instance isn't created!");
        }
        return interfaceClazz;
    }
}
