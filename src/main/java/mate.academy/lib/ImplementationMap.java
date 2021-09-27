package mate.academy.lib;

import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;

import java.util.HashMap;
import java.util.Map;

public class ImplementationMap {
    private static final Map<Class<?>, Class<?>> interfaceImplementations
            = Map.of(FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductParserImpl.class);

    public static Class<?> get(Class<?> interfaceClass) {
        return interfaceImplementations.get(interfaceClass);
    }

}
