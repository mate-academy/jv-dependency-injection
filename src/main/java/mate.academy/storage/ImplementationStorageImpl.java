package mate.academy.storage;

import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class ImplementationStorageImpl implements Storage<Class<?>, Class<?>> {
    private static Map<Class<?>, Class<?>> implementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    @Override
    public Class<?> get(Class<?> key) {
        return implementations.get(key);
    }

    @Override
    public boolean isPresent(Class<?> key) {
        return implementations.containsKey(key);
    }
}
