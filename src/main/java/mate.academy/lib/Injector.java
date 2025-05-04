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
    private static final Map<Class<?>, Class<?>> interfaceImpl
            = Map.of(FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
            );
    private static final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> classImpl = findImplementation(interfaceClazz);
        if (!classImpl.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class: " + interfaceClazz.getName()
            + " not marked as @Component");
        }
        Object clazzImplInstance = null;
        Field[] fields = classImpl.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplInstance = createNewInstance(classImpl);
                field.setAccessible(true);
                try {
                    field.set(clazzImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t initialize field value");
                }
            }
        }
        if (clazzImplInstance == null) {
            clazzImplInstance = createNewInstance(classImpl);
        }
        return clazzImplInstance;
    }

    private Object createNewInstance(Class<?> classImpl) {
        if (instances.containsKey(classImpl)) {
            return instances.get(classImpl);
        }
        try {
            Constructor<?> constructor = classImpl.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(classImpl, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can`t create a new instance of"
            + classImpl.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        return interfaceImpl.get(interfaceClazz);
    }
}
