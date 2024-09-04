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

    static {
        interfaceToImplementation.put(ProductService.class, ProductServiceImpl.class);
        interfaceToImplementation.put(ProductParser.class, ProductParserImpl.class);
        interfaceToImplementation.put(FileReaderService.class, FileReaderServiceImpl.class);
    }

    public Class<?> getImplementation(Class<?> interfaceClazz) {
        return interfaceToImplementation.get(interfaceClazz);
    }
}
