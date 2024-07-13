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
    private final Map<Class<?>, Object> instances = new HashMap<>(
            Map.of(
                    FileReaderService.class, new FileReaderServiceImpl(),
                    ProductParser.class, new ProductParserImpl(),
                    ProductService.class, new ProductServiceImpl()
            )
    );

    public static Injector getInjector() {
        return INJECTOR;
    }

    public <T> T getInstance(Class<T> interfaceClass) {
        if (instances.containsKey(interfaceClass)) {
            return (T) instances.get(interfaceClass);
        }

        for (Class<?> clazz : instances.keySet()) {
            if (interfaceClass.isAssignableFrom(clazz)
                    && clazz.isAnnotationPresent(Component.class)) {
                try {
                    T instance = (T) createInstance(clazz);
                    instances.put(interfaceClass, instance);
                    return instance;
                } catch (ExceptionInInitializerError e) {
                    throw new RuntimeException("Can't create object of the class", e);
                }
            }
        }
        throw new RuntimeException("No implementation found for " + interfaceClass.getName());
    }

    private <T> T createInstance(Class<T> clazz) {
        T instance;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create object of the class", e);
        }

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                try {
                    Class<?> fieldType = field.getType();
                    Object fieldInstance = getInstance(fieldType);
                    field.setAccessible(true);
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot get access to the field ", e);
                }
            }
        }

        return instance;
    }
}
