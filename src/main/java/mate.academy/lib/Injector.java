package mate.academy.lib;

import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzImplInstance = createNewInstance(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            try {
                if (field.getType().isInterface()) {
                    Object createdInstance = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(clazzImplInstance, createdInstance);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Can't initialize field value. Class: "
                        + clazz.getName() + ". Field: " + field.getName());
            }
        }
        return clazzImplInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            if (clazz.getAnnotation(Component.class) == null) {
                throw new RuntimeException("This class doesn't have annotation 'Component': "
                        + clazz.getName());
            }
            Object createdInstance = clazz.getConstructor().newInstance();
            instances.put(clazz, createdInstance);
            return createdInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfacesImpl = Map.of(
                FileReaderService.class, FileReaderServiceImpl.class,
                ProductParser.class, ProductParserImpl.class,
                ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return interfacesImpl.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
