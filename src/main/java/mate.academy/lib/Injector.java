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
    private Map<Class<?>, Class<?>> interfaceImplMap = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (!interfaceImplMap.containsKey(interfaceClazz)) {
            throw new RuntimeException("No implementation found for interface: " + interfaceClazz);
        }

        Class<?> clazz = interfaceImplMap.get(interfaceClazz);

        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz + " is missing @Component annotation");
        }

        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        Object instance = createInstance(clazz);
        injectDependencies(instance);

        instances.put(clazz, instance);
        return instance;
    }

    private Object createInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create instance of class: " + clazz, e);
        }
    }

    private void injectDependencies(Object instance) {
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Class<?> fieldClass = field.getType();
                Object dependency = getInstance(fieldClass);

                try {
                    field.setAccessible(true);
                    field.set(instance, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject dependency into field: "
                            + field, e);
                }
            }
        }
    }
}
