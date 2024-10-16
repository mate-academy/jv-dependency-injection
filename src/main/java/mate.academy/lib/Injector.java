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
    private static final Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();

    static {
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
    }

    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public <T> T getInstance(Class<T> interfaceClazz) {
        Class<?> implementationClass = interfaceImplementations.get(interfaceClazz);
        if (implementationClass == null) {
            throw new RuntimeException("No implementation found for " + interfaceClazz.getName());
        }
        if (!implementationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + implementationClass.getName()
                + " doesn't have @Component");
        }
        if (instances.containsKey(interfaceClazz)) {
            return (T) instances.get(interfaceClazz);
        }
        try {
            Object instance = implementationClass.getDeclaredConstructor().newInstance();
            instances.put(interfaceClazz, instance);
            Field[] fields = implementationClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Class<?> fieldType = field.getType();
                    Object fieldInstance = getInstance(fieldType);
                    field.setAccessible(true);
                    field.set(instance, fieldInstance);
                }
            }
            return (T) instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create an instance of "
                + interfaceClazz.getName(), e);
        }
    }
}
