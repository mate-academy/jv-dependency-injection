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

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzImplementationInstance = createNewInstance(clazz);
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = createNewInstance(findImplementation(field.getType()));
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Component.class)) {
            try {
                Constructor<?> constructor = clazz.getConstructor();
                return constructor.newInstance();
            } catch (NoSuchMethodException | InvocationTargetException
                     | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Can`t create new instance of " + clazz.getName());
            }
        } else {
            throw new RuntimeException("Can`t create an instance of the class."
                    + "There is no annotation");
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> implementations = new HashMap<>();
        implementations.put(ProductService.class, ProductServiceImpl.class);
        implementations.put(ProductParser.class, ProductParserImpl.class);
        implementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return implementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
