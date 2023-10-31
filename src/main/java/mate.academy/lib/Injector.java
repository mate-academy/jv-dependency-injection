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
    private final Map<Class<?>, Object> instances;
    private final Map<Class<?>, Class<?>> interfaceImplementations;

    private Injector() {
        instances = new HashMap<>();
        interfaceImplementations = Map.of(
                ProductParser.class, ProductParserImpl.class,
                ProductService.class, ProductServiceImpl.class,
                FileReaderService.class, FileReaderServiceImpl.class
        );
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            throw new IllegalArgumentException("Only interfaces are supported for injection");
        }

        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }

        if (!interfaceImplementations.containsKey(interfaceClazz)) {
            throw new IllegalArgumentException("No implementation found for interface: "
                    + interfaceClazz.getName());
        }

        Class<?> implementationClazz = interfaceImplementations.get(interfaceClazz);
        try {
            Object instance = implementationClazz.getDeclaredConstructor().newInstance();
            injectDependencies(instance);
            instances.put(interfaceClazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Injection failed, missing @Component annotation on the class "
                    + implementationClazz.getName(), e);
        }
    }

    private void injectDependencies(Object instance) throws IllegalAccessException {
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Class<?> fieldType = field.getType();
                Object fieldInstance = getInstance(fieldType);
                field.setAccessible(true);
                field.set(instance, fieldInstance);
            }
        }
    }
}
