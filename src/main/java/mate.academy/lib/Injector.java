package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import mate.academy.exception.InjectException;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private static final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new InjectException("Can not inject this class: " + clazz
            + ". This class not contains Component annotation!");
        }
        Field[] declaredFields = clazz.getDeclaredFields();
        Object clazzImplementationInstance = null;
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new InjectException("Can not initialize field value. "
                    + "Class: " + clazz.getName() + "Field: " + field.getName() + e);
                }

            }
        }

        return clazzImplementationInstance != null
                ? clazzImplementationInstance : createNewInstance(clazz);
    }

    public Object createNewInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }
        try {
            Object newInstance = interfaceClazz.getConstructor().newInstance();
            instances.put(interfaceClazz, newInstance);
            return newInstance;
        } catch (ReflectiveOperationException e) {
            throw new InjectException("Can not create new instance of class: "
                    + interfaceClazz.getName() + e);
        }
    }

    public Class<?> findImplementation(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            return interfaceClazz;
        }
        return interfaceImplementations.get(interfaceClazz);
    }
}
