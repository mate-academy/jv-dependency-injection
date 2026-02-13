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

    private final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class
    );

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementationClass = interfaceClazz;

        if (interfaceClazz.isInterface()) {
            implementationClass = interfaceImplementations.get(interfaceClazz);

            if (implementationClass == null) {
                throw new RuntimeException("No implementation found for interface: "
                        + interfaceClazz.getName());
            }
        }

        if (!implementationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Class " + implementationClass.getName()
                            + " is not annotated with @Component");
        }

        if (instances.containsKey(implementationClass)) {
            return instances.get(implementationClass);
        }

        try {
            Object instance = implementationClass
                    .getDeclaredConstructor()
                    .newInstance();

            for (Field field : implementationClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    Object dependency = getInstance(field.getType());
                    field.set(instance, dependency);
                }
            }

            instances.put(implementationClass, instance);

            return instance;

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Cannot create instance of "
                            + implementationClass.getName(), e);
        }
    }
}
