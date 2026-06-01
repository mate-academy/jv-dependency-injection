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
    private final Map<Class<?>, Class<?>> interfaceImplementations =
            Map.of(ProductService.class, ProductServiceImpl.class,
                    ProductParser.class, ProductParserImpl.class,
                    FileReaderService.class, FileReaderServiceImpl.class);

    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = interfaceImplementations.get(interfaceClazz);

        if (clazz == null) {
            throw new RuntimeException("Interface realization "
                    + interfaceClazz.getName()
                    + " not found");
        }

        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed, missing "
                    + "@Component annotation on the class "
                    + clazz.getName());
        }

        Object instance;

        try {
            instance = clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());

                    field.setAccessible(true);

                    field.set(instance, fieldInstance);
                }
            }
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("Couldn't create class example "
            + clazz.getName(), exception);
        }

        instances.put(clazz, instance);
        return instance;
    }
}
