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
    private final Map<Class<?>, Object> instanceCache = new HashMap<>();

    private final Map<Class<?>, Class<?>> interfaceToImpl = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implClass = interfaceToImpl.get(interfaceClazz);
        if (implClass == null) {
            throw new RuntimeException("No implementation mapped for " + interfaceClazz.getName());
        }

        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + implClass.getName()
                    + " is not annotated with @Component");
        }

        if (instanceCache.containsKey(implClass)) {
            return instanceCache.get(implClass);
        }

        try {
            Object instance = implClass.getDeclaredConstructor().newInstance();

            for (Field field : implClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Class<?> fieldType = field.getType();
                    Object dependency = getInstance(fieldType);
                    field.setAccessible(true);
                    field.set(instance, dependency);
                }
            }

            instanceCache.put(implClass, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of: " + implClass.getName(), e);
        }
    }
}
