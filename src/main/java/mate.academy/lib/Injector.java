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

    private Map<Class<?>, Class<?>> interfaceClazzMap =
            Map.of(ProductService.class, ProductServiceImpl.class,
                    ProductParser.class, ProductParserImpl.class,
                    FileReaderService.class, FileReaderServiceImpl.class);

    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = interfaceClazzMap.get(interfaceClazz);

        if (clazz == null) {
            clazz = interfaceClazz;
        }

        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed, missing "
                    + "@Component annotation on the class " + clazz.getName());
        }

        Object instance = instances.get(clazz);
        if (instance != null) {
            return instance;
        }
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
            instances.put(clazz, instance);
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(instance, fieldInstance);
                }
            }
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cant create an instance of " + clazz.getName());
        }
    }
}
