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
    private static final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Cant initialize instance of class " + interfaceClazz.getSimpleName());
        }
        Object clazzImplInstance = createNewInstance(clazz);

        for (Field declaredField : clazz.getDeclaredFields()) {
            if (declaredField.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(declaredField.getType());
                declaredField.setAccessible(true);
                try {
                    declaredField.set(clazzImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t set value for " + declaredField.getName(), e);
                }
            }
        }
        instances.put(clazz, clazzImplInstance);
        return clazzImplInstance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImpl = new HashMap<>();
        interfaceImpl.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImpl.put(ProductService.class, ProductServiceImpl.class);
        interfaceImpl.put(ProductParser.class, ProductParserImpl.class);
        if (interfaceImpl.containsKey(interfaceClazz)) {
            return interfaceImpl.get(interfaceClazz);
        }
        return interfaceClazz;
    }

    private Object createNewInstance(Class<?> clazz) {
        Object clazzImplInstance;
        try {
            Constructor<?> constructor = clazz.getConstructor();
            clazzImplInstance = constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException
                 | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Can`t create new instance of " + clazz.getSimpleName(), e);
        }
        return clazzImplInstance;
    }
}
