package mate.academy.lib;

import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();

    private static final Map<Class<?>, Class<?>> IMPLEMENTATIONS = Map.of(
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
        try {
            if (!IMPLEMENTATIONS.containsKey(interfaceClazz)) {
                throw new RuntimeException("No implementation found for interface: "
                        + interfaceClazz.getName());
            }

            Class<?> implClazz = IMPLEMENTATIONS.get(interfaceClazz);
            if (!implClazz.isAnnotationPresent(Component.class)) {
                throw new RuntimeException("Class " + implClazz.getName()
                        + " is not annotated with @Component");
            }

            Object implInstance = implClazz.getDeclaredConstructor().newInstance();
            for (var field : implClazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object dependency = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(implInstance, dependency);
                }
            }
            return implInstance;
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of class: "
                    + interfaceClazz.getName(), e);
        }
    }
}
