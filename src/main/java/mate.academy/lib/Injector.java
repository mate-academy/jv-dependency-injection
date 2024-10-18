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
        if (!interfaceClazz.isAnnotationPresent(Component.class) && !interfaceClazz.isInterface()) {
            throw new RuntimeException("Class " + interfaceClazz.getName()
                    + " is not marked as @Component");
        }

        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }

        Object instance = createInstance(interfaceClazz);
        instances.put(interfaceClazz, instance);
        return instance;
    }

    private Object createInstance(Class<?> clazz) {
        try {
            if (clazz.isInterface()) {
                clazz = findImplementation(clazz);
            }

            if (!clazz.isAnnotationPresent(Component.class)) {
                throw new RuntimeException("Class " + clazz.getName()
                        + " is not marked as @Component");
            }

            Object instance = clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    Object fieldInstance = getInstance(field.getType());
                    field.set(instance, fieldInstance);
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of class " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceToImplementation = new HashMap<>();
        interfaceToImplementation.put(ProductService.class, ProductServiceImpl.class);
        interfaceToImplementation.put(ProductParser.class, ProductParserImpl.class);
        interfaceToImplementation.put(FileReaderService.class, FileReaderServiceImpl.class);

        if (interfaceToImplementation.containsKey(interfaceClazz)) {
            return interfaceToImplementation.get(interfaceClazz);
        }
        throw new RuntimeException("No implementation found for " + interfaceClazz.getName());
    }
}
