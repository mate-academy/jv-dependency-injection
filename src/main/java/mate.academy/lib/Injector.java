package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {

    private static final Map<Class<?>, Class<?>> interfaceToImpl = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new ConcurrentHashMap<>();

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> clazz) {
        if (instances.containsKey(clazz)) {
            return (T) instances.get(clazz);
        }

        Class<?> implClass = clazz;
        if (clazz.isInterface()) {
            implClass = interfaceToImpl.get(clazz);
            if (implClass == null) {
                throw new RuntimeException("No implementation found for interface "
                        + clazz.getName());
            }
        }

        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't create instance of "
                    + implClass.getName()
                    + " - missing @Component");
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

            instances.put(clazz, instance);
            return (T) instance;
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of " + implClass.getName(), e);
        }
    }
}
