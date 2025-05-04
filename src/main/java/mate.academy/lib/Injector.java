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
    private static final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object injectedObject = null;
        Class<?> type = findTypeImplementationFor(interfaceClazz);
        if (type == null) {
            throw new RuntimeException("Don't have an implementation for the type: "
                    + interfaceClazz.getName());
        }
        if (!type.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(interfaceClazz.getName()
                    + ": this class hasn't annotation 'Component'.");
        }
        Field[] fields = type.getDeclaredFields();
        for (Field field: fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                injectedObject = createNewInstance(type);
                field.setAccessible(true);
                try {
                    field.set(injectedObject, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't inject the object "
                            + fieldInstance.getClass().getName()
                            + " to field " + field.getName(), e);
                }
            }
        }
        if (injectedObject == null) {
            injectedObject = createNewInstance(type);
        }
        return injectedObject;
    }

    private Object createNewInstance(Class<?> type) {
        if (instances.containsKey(type)) {
            return instances.get(type);
        }
        try {
            Object instance = type.getConstructor().newInstance();
            instances.put(type, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create new instance of type: " + type.getName(), e);
        }
    }

    private Class<?> findTypeImplementationFor(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> implementations = new HashMap<>();
        implementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementations.put(ProductParser.class, ProductParserImpl.class);
        implementations.put(ProductService.class, ProductServiceImpl.class);
        return implementations.get(interfaceClazz);
    }
}
