package mate.academy.lib;

import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();
    private static Map<Class<?>,Object> mapInstances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public static Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzImplementationInstance = null;
        Field[] declaredFields = clazz.getDeclaredFields();

        return null;
    }

    private static Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>,Class<?>> mapImplementations = new HashMap<>();
        mapImplementations.put(ProductService.class, ProductServiceImpl.class);
        mapImplementations.put(ProductParser.class, ProductParserImpl.class);
        mapImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return mapImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
