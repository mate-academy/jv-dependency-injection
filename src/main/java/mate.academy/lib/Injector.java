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
    private final Map<Class<?>, Class<?>> interfaceImplementationMap = Map
            .of(ProductService.class, ProductServiceImpl.class,
                    ProductParser.class, ProductParserImpl.class,
                    FileReaderService.class, FileReaderServiceImpl.class);

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            throw new RuntimeException("Expected an interface, but got: "
                    + interfaceClazz.getName());
        }
        Class<?> implementationClass = interfaceImplementationMap.get(interfaceClazz);
        if (implementationClass == null) {
            throw new RuntimeException("No implementation found for interface: "
                    + interfaceClazz.getName());
        }
        if (!implementationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed, "
                    + "missing @Component annotation on the class "
                    + implementationClass.getName());
        }

        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }

        Object instance = createInstance(implementationClass);
        instances.put(interfaceClazz, instance);
        return instance;
    }

    private Object createInstance(Class<?> clazz) {
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    Object fieldInstance = getInstance(field.getType());
                    field.set(instance, fieldInstance);
                }
            }
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Injection failed for " + clazz.getName(), e);
        }
    }
}



