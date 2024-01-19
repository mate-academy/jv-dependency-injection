package mate.academy.lib;

import java.util.HashMap;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class InterfaceImplementationsStorage {
    private static final Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();

    static {
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
    }

    public static Class<?> getImplementationClass(Class<?> interfaceClazz) {
        return interfaceImplementations.get(interfaceClazz);
    }
}
