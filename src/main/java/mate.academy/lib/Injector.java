package mate.academy.lib;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public <T> T getInstance(Class<T> interfaceClass) {
        if (instances.containsKey(interfaceClass)) {
            return interfaceClass.cast(instances.get(interfaceClass));
        }

        for (Class<?> clazz : getAllClasses()) {
            if (interfaceClass.isAssignableFrom(clazz)
                    && clazz.isAnnotationPresent(Component.class)) {
                try {
                    T instance = (T) createInstance(clazz);
                    instances.put(interfaceClass, instance);
                    return instance;
                } catch (Exception e) {
                    throw new RuntimeException("Can't create object of the class", e);
                }
            }
        }
        throw new RuntimeException("No implementation found for " + interfaceClass.getName());
    }

    private <T> T createInstance(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz.getName()
                    + " is not annotated with @Component");
        }

        T instance;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException
                 | InvocationTargetException | NoSuchMethodException e) {
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
                    throw new RuntimeException(e);
                }
            }
        }

        return instance;
    }

    private List<Class<?>> getAllClasses() {
        return List.of(
                FileReaderServiceImpl.class,
                ProductParserImpl.class,
                ProductServiceImpl.class
        );
    }
}
