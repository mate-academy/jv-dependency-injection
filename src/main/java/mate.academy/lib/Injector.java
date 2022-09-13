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
        Object clazzImplInst = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Annotation @Component is missing in class: "
                    + clazz.getName());
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field: fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplInst = createNewInst(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplInst, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class: "
                            + clazz.getName()
                            + " Field"
                            + field.getName());
                }
            }
        }
        if (clazzImplInst == null) {
            clazzImplInst = createNewInst(clazz);
        }
        return clazzImplInst;
    }

    private Object createNewInst(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz,instance);
            return instances.get(clazz);
        } catch (NoSuchMethodException
                | InvocationTargetException
                | InstantiationException
                | IllegalAccessException e) {
            throw new RuntimeException("Can not create a new instance: "
                    + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceAndImpl = new HashMap<>();
        interfaceAndImpl.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceAndImpl.put(ProductService.class, ProductServiceImpl.class);
        interfaceAndImpl.put(ProductParser.class, ProductParserImpl.class);
        if (!interfaceAndImpl.containsKey(interfaceClazz)
                && !interfaceAndImpl.containsValue(interfaceClazz)) {
            throw new RuntimeException("Wrong class: " + interfaceClazz.getName());
        }
        if (interfaceClazz.isInterface()) {
            return interfaceAndImpl.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
