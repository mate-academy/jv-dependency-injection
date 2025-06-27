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

    private final Map<Class<?>, Object> instances = new HashMap<>();

    static {
        interfaceImplementations = Map.of(
                FileReaderService.class, FileReaderServiceImpl.class,
                ProductParser.class, ProductParserImpl.class,
                ProductService.class, ProductServiceImpl.class
        );
    }

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }

        Class<?> implementationClass = getImplementation(interfaceClazz);

        if (!implementationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + implementationClass.getName()
                    + " is not annotated with @Component");
        }

        Object instance = createNewInstance(implementationClass);
        instances.put(interfaceClazz, instance);

        return instance;
    }

    private Class<?> getImplementation(Class<?> interfaceClazz) {
        Class<?> implementationClass = interfaceImplementations.get(interfaceClazz);

        if (implementationClass == null) {
            throw new RuntimeException("No implementation found for interface "
                    + interfaceClazz.getName());
        }

        return implementationClass;
    }

    private Object createNewInstance(Class<?> implementationClass) {
        try {
            Constructor<?> constructor = implementationClass.getConstructor();
            Object instance = constructor.newInstance();
            Field[] declaredFields = implementationClass.getDeclaredFields();

            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(instance, fieldInstance);
                }
            }

            return instance;

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cannot create instance of "
                    + implementationClass.getName(), e);
        }
    }
}
