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
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instancesMap;

    private Injector() {
        instancesMap = new HashMap<>();
        instancesMap.put(FileReaderService.class, new FileReaderServiceImpl());
        instancesMap.put(ProductParser.class, new ProductParserImpl());
        instancesMap.put(ProductService.class, new ProductServiceImpl());
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (!instancesMap.containsKey(interfaceClazz)) {
            throw new RuntimeException("Injection failed, missing @Component annotation for class: "
                    + interfaceClazz.getName());
        }
        return instancesMap.get(interfaceClazz);
    }
}
