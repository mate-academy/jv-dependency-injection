package mate.academy.lib;

import java.util.HashMap;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Map<Class<?>, Class<?>> interfaceToImplementation = new HashMap<>();
    private static final Injector injector = new Injector();

    static {
        interfaceToImplementation.put(ProductService.class, ProductServiceImpl.class);
        interfaceToImplementation.put(ProductParser.class, ProductParserImpl.class);
        interfaceToImplementation.put(FileReaderService.class, FileReaderServiceImpl.class);
    }

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public <T> T getInstance(Class<T> interfaceClazz) {
        Class<?> implClass = interfaceToImplementation.get(interfaceClazz);
        if (implClass == null) {
            throw new RuntimeException("No implementation found for " + interfaceClazz.getName());
        }
        try {
            return (T) implClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of " + implClass.getName(), e);
        }
    }
}
