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
    private static final Map<Class<?>, Class<?>> interfaceToImplementation = new HashMap<>();
    private static final Map<Class<?>, Object> instances = new HashMap<>();
    private static final Injector injector = new Injector();

    static {
        interfaceToImplementation.put(ProductService.class, ProductServiceImpl.class);
        interfaceToImplementation.put(ProductParser.class, ProductParserImpl.class);
        interfaceToImplementation.put(FileReaderService.class, FileReaderServiceImpl.class);
    }

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public <T> T getInstance(Class<T> interfaceClazz) {
        Class<?> implClass = interfaceToImplementation.get(interfaceClazz);
        if (implClass == null) {
            throw new RuntimeException("No implementation found for " + interfaceClazz.getName());
        }
        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Implementation " + implClass.getName() + " is not annotated with @Component");
        }
        if (instances.containsKey(implClass)) {
            return (T) instances.get(implClass);
        }
        try {
            T instance = (T) implClass.getDeclaredConstructor().newInstance();
            for (Field field : implClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    field.set(instance, getInstance(field.getType()));
                }
            }
            instances.put(implClass, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of " + implClass.getName(), e);
        }
    }
}
