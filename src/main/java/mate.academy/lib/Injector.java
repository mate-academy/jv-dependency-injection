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

    private final Map<Class<?>, Class<?>> implementations =
            Map.of(
                    FileReaderService.class, FileReaderServiceImpl.class,
                    ProductService.class, ProductServiceImpl.class,
                    ProductParser.class, ProductParserImpl.class
            );

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (!interfaceClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Missing Component annotation");
        }
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }
        try {
            Object instance = implementations.get(interfaceClazz)
                    .getDeclaredConstructor()
                    .newInstance();
            for (Field field : interfaceClazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(instance, fieldInstance);
                }
            }
            instances.put(interfaceClazz, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create an instance of "
                    + interfaceClazz.getName(), e);
        }
    }
}
