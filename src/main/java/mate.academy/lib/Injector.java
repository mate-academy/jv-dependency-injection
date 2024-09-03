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
    private static final Map<Class<?>, Class<?>> interfaceImplementations;
    private final Map<Class<?>, Object> components = new HashMap<>();

    static {
        interfaceImplementations = Map.of(
                FileReaderService.class, FileReaderServiceImpl.class,
                ProductService.class, ProductServiceImpl.class,
                ProductParser.class, ProductParserImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object instance = null;
        Class<?> clazz = getImplementation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                instance = createNewInstance(clazz);

                try {
                    field.setAccessible(true);
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class "
                            + clazz.getName() + ". Field: " + field.getName());
                }
            }
        }
        if (instance == null) {
            instance = createNewInstance(clazz);
        }
        return instance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class is not annotated with @Component "
                    + clazz.getName());
        } else {
            if (components.containsKey(clazz)) {
                return components.get(clazz);
            }

            try {
                Constructor<?> constructor = clazz.getConstructor();
                Object newInstance = constructor.newInstance();
                components.put(clazz, newInstance);
                return newInstance;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Can't create a new instance of "
                        + clazz.getName());
            }
        }
    }

    private static Class<?> getImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
