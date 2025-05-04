package mate.academy.lib;

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
    private static final Map<Class<?>, Class<?>> implementstionMap = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("@Component missing before class declaration!");
        }

        Object clazzImplementationInstance = createNewInstance(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class: "
                                               + clazz.getName() + ". Field: " + field.getName());
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }
        try {
            Object instance = interfaceClazz.getConstructor().newInstance();
            instances.put(interfaceClazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of "
                                       + interfaceClazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            return interfaceClazz;
        }
        return implementstionMap.get(interfaceClazz);
    }
}
