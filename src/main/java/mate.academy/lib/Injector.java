package mate.academy.lib;

import java.lang.reflect.Field;
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
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }

        Class<?> implClass = findImplementation(interfaceClazz);

        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("No @Component annotation on class: " + implClass.getName());
        }

        try {
            Object implInstance = implClass.getDeclaredConstructor().newInstance();

            for (Field field : implClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object dependency = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(implInstance, dependency);
                }
            }

            instances.put(interfaceClazz, implInstance);
            return implInstance;
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of " + implClass.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.equals(FileReaderService.class)) {
            return FileReaderServiceImpl.class;
        }
        if (interfaceClazz.equals(ProductParser.class)) {
            return ProductParserImpl.class;
        }
        if (interfaceClazz.equals(ProductService.class)) {
            return ProductServiceImpl.class;
        }
        throw new RuntimeException("Unsupported class: " + interfaceClazz.getName());
    }
}
