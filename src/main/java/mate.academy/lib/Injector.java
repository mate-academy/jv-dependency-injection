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

    private final Map<Class<?>, Class<?>> interfaceToImpl = Map.of(
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class
    );

    private final Map<Class<?>, Object> instanceCache = new HashMap<>();

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implClass;
        if (interfaceClazz.isInterface()) {
            implClass = interfaceToImpl.get(interfaceClazz);
            if (implClass == null) {
                throw new RuntimeException("No implementation found for "
                        + interfaceClazz.getName());
            }
        } else {
            implClass = interfaceClazz;
        }

        if (instanceCache.containsKey(implClass)) {
            return instanceCache.get(implClass);
        }

        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Implementation " + implClass.getName()
                    + " is not annotated with @Component");
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

            instanceCache.put(implClass, instance);
            return instance;

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of class: "
                    + implClass.getName(), e);
        }
    }
}
