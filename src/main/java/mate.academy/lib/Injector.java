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
    private final Map<Class<?>, Class<?>> implementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );
    private final Map<Class<?>, Object> instancesCache = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementationClass = implementations.get(interfaceClazz);
        if (implementationClass == null
                || !implementationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed, missing @Component implementation for "
                    + interfaceClazz.getName());
        }
        Object savedInstance = instancesCache.get(implementationClass);
        if (savedInstance != null) {
            return savedInstance;
        }
        try {
            Object newInstance = implementationClass.getDeclaredConstructor().newInstance();
            instancesCache.put(implementationClass, newInstance);
            for (Field field : implementationClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(newInstance, fieldInstance);
                }
            }
            return newInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of "
                    + implementationClass.getName(), e);
        }
    }
}
