package mate.academy.lib;

import java.lang.reflect.Constructor;
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
            throw new RuntimeException("Unsupported class: " + interfaceClazz.getName());
        }
        Object clazzImplementationInstance = null;
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                try {
                    Object instance = getInstance(field.getType());
                    clazzImplementationInstance = createNewInstance(clazz);
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, instance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't create an instance");
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        Constructor<?> constructor;
        Object instance;
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            constructor = clazz.getConstructor();
            instance = constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create an instance of " + clazz.getName(), e);
        }
        instances.put(clazz, instance);
        return instance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> implementations = new HashMap<>();
        implementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementations.put(ProductParser.class, ProductParserImpl.class);
        implementations.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return implementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
