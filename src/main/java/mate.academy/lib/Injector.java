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
    private Map<Class<?>, Object> instances = new HashMap<>();

    private Map<Class<?>, Class<?>> implementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    public static Injector getInjector() {

        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }

        Class<?> implementationClazz = findImplementation(interfaceClazz);

        if (!implementationClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class is not component");
        }
        try {
            Object instance = implementationClazz.getDeclaredConstructor().newInstance();

            Field[] fields = implementationClazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Class<?> fieldType = field.getType();
                    Object dependency = getInstance(fieldType);

                    field.setAccessible(true);
                    field.set(instance, dependency);
                }
            }
            instances.put(interfaceClazz, instance);
            return instance;

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of "
                    + implementationClazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Class<?> implementation = implementations.get(interfaceClazz);

        if (implementation == null) {
            throw new RuntimeException("Can't find implementation for "
                    + interfaceClazz.getName());

        }
        return implementation;
    }
}
