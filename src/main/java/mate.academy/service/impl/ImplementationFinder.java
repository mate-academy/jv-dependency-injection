package mate.academy.service.impl;

import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;

public class ImplementationFinder {
    private static final Map<Class<?>, Class<?>> IMPLEMENTATION_MAP = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    public static Class<?> getImplementation(Class<?> clazz) {
        return IMPLEMENTATION_MAP.get(clazz);
    }
}
