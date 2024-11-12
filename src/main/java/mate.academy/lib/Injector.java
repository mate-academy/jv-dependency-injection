package mate.academy.lib;

import java.lang.reflect.Constructor;
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
    private Map<Class<?>, Object> instances = new HashMap<>();

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implClass = findImplementation(interfaceClazz);
        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + implClass.getName()
                    + " is not marked with @Component");
        }

        if (instances.containsKey(implClass)) {
            return instances.get(implClass);
        }

        try {
            Constructor<?> constructor = implClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object instance = constructor.newInstance();

            for (Field field : implClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object dependency = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(instance, dependency);
                }
            }

            instances.put(implClass, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of "
                    + implClass.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz == ProductService.class) {
            return ProductServiceImpl.class;
        }
        if (interfaceClazz == ProductParser.class) {
            return ProductParserImpl.class;
        }
        if (interfaceClazz == FileReaderService.class) {
            return FileReaderServiceImpl.class;
        }
        throw new RuntimeException("No implementation found for "
                + interfaceClazz.getName());
    }
}
