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
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> interfaceToImpl = Map.of(
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
        return createInstance(interfaceClazz);
    }

    private Object createInstance(Class<?> clazz) {
        try {
            if (clazz.isInterface()) {
                clazz = interfaceToImpl.get(clazz);
                if (clazz == null) {
                    throw new RuntimeException("No implementation found for " + clazz.getName());
                }
            }

            if (!clazz.isAnnotationPresent(Component.class)) {
                throw new RuntimeException("Class " + clazz.getName()
                        + " is missing @Component annotation");
            }

            Constructor<?> constructor;
            try {
                constructor = clazz.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Class " + clazz.getName()
                        + " must have a no-argument constructor", e);
            }

            Object instance = constructor.newInstance();
            injectDependencies(instance);
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create an instance of class "
                    + clazz.getName(), e);
        }
    }

    private void injectDependencies(Object instance) throws IllegalAccessException {
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Class<?> fieldType = field.getType();
                Object dependency = getInstance(fieldType);
                field.setAccessible(true);
                field.set(instance, dependency);
            }
        }
    }
}
