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

    private static final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    private final Map<Class<?>, Object> instances = new HashMap<>();

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementationClass = interfaceImplementations.get(interfaceClazz);
        if (implementationClass == null) {
            throw new RuntimeException("No implementation found for interface: "
                    + interfaceClazz.getName());
        }
        return createInstance(implementationClass);
    }

    private Object createInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Injection failed: missing @Component annotation on class "
                            + clazz.getName());
        }

        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            instances.put(clazz, instance);

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Class<?> dependencyInterface = field.getType();
                    Class<?> dependencyImpl =
                            interfaceImplementations.get(dependencyInterface);

                    if (dependencyImpl == null) {
                        throw new RuntimeException("No implementation found for dependency: "
                                + dependencyInterface.getName());
                    }

                    Object dependencyObject = createInstance(dependencyImpl);

                    field.setAccessible(true);
                    field.set(instance, dependencyObject);
                }
            }

            return instance;

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Injection failed when creating instance of " + clazz.getName(), e);
        }
    }
}
