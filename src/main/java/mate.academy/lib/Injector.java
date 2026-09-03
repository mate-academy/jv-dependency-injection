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

    // Мапа інтерфейс → імплементація
    private final Map<Class<?>, Class<?>> implementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }

        Class<?> implClass = implementations.get(interfaceClazz);
        if (implClass == null) {
            throw new RuntimeException("Unsupported class: " + interfaceClazz.getName());
        }

        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("No @Component annotation on class: " + implClass.getName());
        }

        try {
            Object implInstance = implClass.getDeclaredConstructor().newInstance();

            for (Field field : implClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object dependency = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(implInstance, dependency);
                }
            }

            instances.put(interfaceClazz, implInstance);
            return implInstance;
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of " + implClass.getName(), e);
        }
    }
}
