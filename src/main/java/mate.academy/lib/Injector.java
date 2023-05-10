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

    public Object getInstance(Class<?> clazz) {
        clazz = clazz.isInterface() ? getTypeOfImpl(clazz) : clazz;
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class isn`t component");
        }
        Object resObject = createNewImpl(clazz);
        for (Field field: clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldObject = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(resObject, fieldObject);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t implement field. Class: "
                            + clazz.getName(), e);
                }
            }
        }
        return resObject;
    }

    private Object createNewImpl(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object object = constructor.newInstance();
            instances.put(clazz, object);
            return object;
        } catch (NoSuchMethodException
                 | InvocationTargetException
                 | InstantiationException
                 | IllegalAccessException e) {
            throw new RuntimeException("Can`t create instance of " + clazz.getName(), e);
        }
    }

    public Class<?> getTypeOfImpl(Class<?> clazz) {
        Map<Class<?>, Class<?>> listImplementations = new HashMap<>();
        listImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        listImplementations.put(ProductParser.class, ProductParserImpl.class);
        listImplementations.put(ProductService.class, ProductServiceImpl.class);
        return listImplementations.get(clazz);
    }
}
