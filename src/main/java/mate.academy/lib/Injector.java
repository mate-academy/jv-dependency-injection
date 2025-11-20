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
    private final Map<Class<?>, Class<?>> impls = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class
    );
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new IllegalStateException("Class must have @Component annotation");
        }
        Object clazzImplInstance = createInstance(clazz);
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object dependency = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(clazzImplInstance, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Couldn't inject field: " + field.getName(), e);
                }
            }
        }
        return clazzImplInstance;
    }

    private Object createInstance(Class<?> clazz) {
        try {
            if (instances.containsKey(clazz)) {
                return instances.get(clazz);
            }
            Object instance = clazz.getConstructor().newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName(), e);
        }
    }

    private <T> Class<?> findImplementation(Class<T> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return impls.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
