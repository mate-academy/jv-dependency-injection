package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    private Injector() {
        registerImplementations();
        injectDependencies();
    }

    public static Injector getInjector() {
        return injector;
    }

    private void registerImplementations() {
        instances.put(ProductService.class, new ProductServiceImpl());
        instances.put(ProductParser.class, new ProductParserImpl());
        instances.put(FileReaderService.class, new FileReaderServiceImpl());
    }

    private void injectDependencies() {
        for (Object instance : instances.values()) {
            for (Field field : instance.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    try {
                        field.set(instance, instances.get(field.getType()));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Failed to inject dependency for field "
                                + field.getName(), e);
                    }
                }
            }
        }
    }

    public <T> T getInstance(Class<T> type) {
        Objects.requireNonNull(type, "Type cannot be null");
        if (instances.containsKey(type)) {
            return type.cast(instances.get(type));
        } else {
            throw new RuntimeException("No implementation found for type: " + type.getName());
        }
    }
}
