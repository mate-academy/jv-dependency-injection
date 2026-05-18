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
    private static final Injector INJECTOR = new Injector();
    private static final Map<Class<?>, Class<?>> INTERFACE_IMPLEMENTATIONS = Map.of(
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class
    );
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return INJECTOR;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = INTERFACE_IMPLEMENTATIONS.get(interfaceClazz);
        if (clazz == null) {
            throw new RuntimeException("Can't find implementation for class: "
                    + interfaceClazz.getName());
        }
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz.getName()
                    + " doesn't have @Component annotation");
        }
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        Object instance = createInstance(clazz);
        instances.put(clazz, instance);
        injectFields(instance, clazz);
        return instance;
    }

    private Object createInstance(Class<?> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of class: "
                    + clazz.getName(), e);
        }
    }

    private void injectFields(Object instance, Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field: "
                            + field.getName(), e);
                }
            }
        }
    }
}
