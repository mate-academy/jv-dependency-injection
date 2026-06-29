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

    private final Map<Class<?>, Class<?>> implementationMap = new HashMap<>();

    private Injector() {
        implementationMap.put(ProductService.class, ProductServiceImpl.class);
        implementationMap.put(ProductParser.class, ProductParserImpl.class);
        implementationMap.put(FileReaderService.class, FileReaderServiceImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementation = implementationMap.get(interfaceClazz);

        if (implementation == null) {
            throw new RuntimeException(
                    "No implementation found for " + interfaceClazz.getName());
        }

        if (!implementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    implementation.getName() + " doesn't have @Component annotation");
        }

        try {
            Object instance = implementation.getDeclaredConstructor().newInstance();

            for (Field field : implementation.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object dependency = getInstance(field.getType());

                    field.setAccessible(true);
                    field.set(instance, dependency);
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of "
                    + implementation.getName(), e);
        }
    }
}
