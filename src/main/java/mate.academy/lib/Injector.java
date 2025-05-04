package mate.academy.lib;

import java.util.Arrays;
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
    private static final Map<Class<?>, Class<?>> objectsImplementations;
    private Map<Class<?>, Object> instances = new HashMap<>();

    static {
        objectsImplementations = Map.of(
                FileReaderService.class, FileReaderServiceImpl.class,
                ProductParser.class, ProductParserImpl.class,
                ProductService.class, ProductServiceImpl.class
        );
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> clazz) {
        Class<?> clazzImpl = getImplementation(clazz);
        Object clazzImplInstance = createNewInstance(clazzImpl);
        instances.put(clazzImpl, clazzImplInstance);
        Arrays.stream(clazzImpl.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Inject.class))
                .forEach(f -> {
                    Object fieldInstance = getInstance(f.getType());
                    try {
                        f.setAccessible(true);
                        f.set(clazzImplInstance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Can't set field value."
                                + "Class: " + clazz.getName() + ". Field: " + f.getName());
                    }
                });
        return clazzImplInstance;
    }

    private Class<?> getImplementation(Class<?> clazz) {
        if (!isValidClazz(clazz)) {
            throw new RuntimeException("Injection failed,"
                    + " missing @Component annotation on the class " + clazz);
        }
        return objectsImplementations.getOrDefault(clazz, clazz);
    }

    private boolean isValidClazz(Class<?> clazz) {
        return clazz.isInterface() || clazz.isAnnotationPresent(Component.class);
    }

    private Object createNewInstance(Class<?> clazz) {
        try {
            return instances.getOrDefault(clazz, clazz.getConstructor().newInstance());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName(), e);
        }
    }
}
