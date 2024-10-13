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
    private final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            ProductService.class, ProductServiceImpl.class, FileReaderService.class,
            FileReaderServiceImpl.class, ProductParser.class, ProductParserImpl.class);
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }
        Class<?> implClass = interfaceImplementations.get(interfaceClazz);
        if (implClass == null) {
            throw new RuntimeException("No implementation found for " + interfaceClazz.getName());
        }

        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class" + implClass.getName()
                    + "is not annotated with @Component");
        }

        Object implInstance;
        try {
            implInstance = implClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create instance of " + implClass.getName(), e);
        }

        for (Field field : implClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Class<?> fieldType = field.getType();
                Object fieldInstance = getInstance(fieldType);

                field.setAccessible(true);
                try {
                    field.set(implInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject dependency into "
                            + field.getName(), e);
                }
            }
        }
        instances.put(interfaceClazz,implInstance);
        return implInstance;
    }
}
