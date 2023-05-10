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
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Cannot create an instance of class " + clazz.getSimpleName()
                    + " if annotation " + Component.class.getSimpleName()
                    + " is missing");
        }
        Object clazzImplementationInstance = null;
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field. Class: "
                            + clazz.getSimpleName()
                            + ", field: " + field.getName(), e);
                }
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
            Object newInstance = clazz.getConstructor().newInstance();
            instances.put(clazz, newInstance);
            return newInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cannot create a new instance of class"
                    + clazz.getSimpleName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> clazz) {
        Map<Class<?>, Class<?>> instances = new HashMap<>();
        instances.put(FileReaderService.class, FileReaderServiceImpl.class);
        instances.put(ProductParser.class, ProductParserImpl.class);
        instances.put(ProductService.class, ProductServiceImpl.class);
        if (instances.get(clazz) == null) {
            return clazz;
        }
        return instances.get(clazz);
    }
}
