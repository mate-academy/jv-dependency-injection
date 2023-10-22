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
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> implementations = initImplementations();

    private Map<Class<?>, Class<?>> initImplementations() {
        return Map.of(
                FileReaderService.class, FileReaderServiceImpl.class,
                ProductService.class, ProductServiceImpl.class,
                ProductParser.class, ProductParserImpl.class
        );
    }

    public static Injector getInjector() {
        return injector;
    }

    private boolean isComponentAnnotated(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class);
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (!isComponentAnnotated(interfaceClazz)) {
            throw new RuntimeException("Missing Component annotation");
        }
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }
        Class<?> implementationClazz = implementations.get(interfaceClazz);
        if (implementationClazz == null) {
            throw new RuntimeException("No implementation found for "
                    + interfaceClazz.getName());
        }
        try {
            Object instance = implementationClazz.getDeclaredConstructor().newInstance();
            for (Field field : implementationClazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    try {
                        Object fieldInstance = getInstance(field.getType());
                        field.setAccessible(true);
                        field.set(instance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Failed to set field " + field.getName()
                                + " in class " + implementationClazz.getName(), e);
                    }
                }
            }
            instances.put(interfaceClazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create an instance of class "
                    + implementationClazz.getName(), e);
        }
    }
}
