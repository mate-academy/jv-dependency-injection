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
    private Map<Class<?>, Object> instances = new HashMap<>();
    private Map<Class<?>,Class<?>> implemetations = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class is not annotated with @Component: "
                    + clazz.getName());
        }
        Class<?> implementation = findImplementation(clazz);
        Object instance = null;
        Field[] fields = implementation.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object newInstance = getInstance(field.getType());
                instance = createNewInstance(implementation);
                field.setAccessible(true);
                try {
                    field.set(instance, newInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can not create an instance", e);
                }
            }
        }
        if (instance == null) {
            instance = createNewInstance(implementation);
        }
        return instance;
    }

    private Object createNewInstance(Class<?> implementation) {
        if (instances.containsKey(implementation)) {
            return instances.get(implementation);
        }

        try {
            Constructor<?> constructor = implementation.getConstructor();
            Object object = constructor.newInstance();
            instances.put(implementation, object);
            return object;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can not create an object", e);
        }
    }

    private Class<?> findImplementation(Class<?> clazz) {
        implemetations.put(FileReaderService.class, FileReaderServiceImpl.class);
        implemetations.put(ProductParser.class, ProductParserImpl.class);
        implemetations.put(ProductService.class, ProductServiceImpl.class);
        if (implemetations.containsKey(clazz)) {
            return implemetations.get(clazz);
        }
        return clazz;
    }
}
