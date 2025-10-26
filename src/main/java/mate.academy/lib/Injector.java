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
    private final Map<Class<?>, Class<?>> implementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazzImpl = implementations.get(interfaceClazz);
        if (clazzImpl == null) {
            throw new RuntimeException("No such implementation for class " + interfaceClazz);
        }
        if (!clazzImpl.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("No @Component annotation found for class "
                    + interfaceClazz);
        }
        if (instances.containsKey(clazzImpl)) {
            return instances.get(clazzImpl);
        }
        try {
            Object instance = clazzImpl.getDeclaredConstructor().newInstance();
            for (Field field : clazzImpl.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Class<?> clazzFieldType = field.getType();
                    Object fieldInstance = getInstance(clazzFieldType);
                    field.setAccessible(true);
                    field.set(instance, fieldInstance);
                }
            }
            instances.put(clazzImpl, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Class " + clazzImpl.getName()
                    + " could not be instantiated", e);
        }
    }
}
