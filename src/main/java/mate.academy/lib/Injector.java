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
        Object clazzImplementationInstance = null;
        Class<?> clazz = findInplementation(interfaceClazz);
        Field[] declaredFields = interfaceClazz.getDeclaredFields();
        for (Field field: declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    clazzImplementationInstance = createNewInstance(clazz);
                    try {
                        field.setAccessible(true);
                        field.set(clazzImplementationInstance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Cant initialize field value.Clazz"
                                + clazz.getName() + " field " + field.getName(), e);
                    }
                }
            } else {
                throw new RuntimeException("shouldn't be able to create an "
                        + "instance of this class in Injector");
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
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
        } catch (InstantiationException | IllegalAccessException
                 | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Can't create a new instance" + clazz.getName());
        }
    }

    private Class<?> findInplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplamentations = new HashMap<>();
        interfaceImplamentations.put(ProductService.class, ProductServiceImpl.class);
        interfaceImplamentations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplamentations.put(ProductParser.class, ProductParserImpl.class);
        if (interfaceClazz.isInterface()) {
            return interfaceImplamentations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
