package mate.academy.lib;

import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class ImplementationMap {
    private static final Map<Class<?>, Class<?>> interfaceImplementations
            = Map.of(FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);

    public static Class<?> get(Class<?> interfaceClass) {
        if (interfaceClass.isInterface()) {
            return interfaceImplementations.get(interfaceClass);
        }
        return interfaceClass;
    }

}
