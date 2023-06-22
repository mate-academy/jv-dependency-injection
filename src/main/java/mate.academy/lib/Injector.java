package mate.academy.lib;

import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();

    public static Injector getInjector() {
        return injector;
    }
    private Map<Class<?>, Object> instanses = new HashMap<>();

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplInstance = createNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value", e);
                }
            }
        }
        if (clazzImplInstance == null) {
            clazzImplInstance = createNewInstance(clazz);
        }
        return clazzImplInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instanses.containsKey(clazz)) {
            return instanses.get(clazz);
        }
        try {
            Object instance = clazz.getConstructor().newInstance();
            instanses.put(clazz, instance);
            return instance;
        } catch (InstantiationException | IllegalAccessException
                 | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Cant create new instance of " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> implementation = new HashMap<>();
        implementation.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementation.put(ProductParser.class, ProductParserImpl.class);
        implementation.put(ProductService.class, ProductServiceImpl.class);
        Class<?> implClass = interfaceClazz.isInterface()
                ? implementation.get(interfaceClazz) : interfaceClazz;
        if (implClass.isAnnotationPresent(Component.class)) {
            return implClass;
        }
        throw new RuntimeException("Injector can't create an instance of class: "
                + implClass.getName());
    }
}
