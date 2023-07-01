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
    private Map<Class<?>, Object> existingInstances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Unsupported class! " + clazz);
        }
        Object currentInstance = null;
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldObject = getInstance(field.getType());
                currentInstance = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(currentInstance, fieldObject);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't set value to next field: " + currentInstance);
                }
            }
        }
        if (currentInstance == null) {
            currentInstance = createNewInstance(clazz);
        }
        return currentInstance;
    }

    private Class<?> findImplementation(Class<?> clazz) {
        Map<Class<?>, Class<?>> implementations = new HashMap<>();
        implementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementations.put(ProductParser.class, ProductParserImpl.class);
        implementations.put(ProductService.class, ProductServiceImpl.class);
        if (!clazz.isInterface()) {
            return clazz;
        }
        if (implementations.containsKey(clazz)) {
            return implementations.get(clazz);
        }
        throw new RuntimeException("There is no implementation for this class: " + clazz);
    }

    private Object createNewInstance(Class<?> clazz) {
        if (existingInstances.containsKey(clazz)) {
            return existingInstances.get(clazz);
        }
        try {
            Object newInstance = clazz.getConstructor().newInstance();
            existingInstances.put(clazz, newInstance);
            return newInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    String.format("Can't create new instance of %s class", clazz.getName()));
        }
    }
}
