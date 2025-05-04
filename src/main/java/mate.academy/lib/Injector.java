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

    private static final Map<Class<?>, Class<?>> interfaceToImplementation = Map.of(
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    private final Map<Class<?>, Object> instances = new HashMap<>();

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public <T> T getInstance(Class<T> interfaceClass) {
        if (instances.containsKey(interfaceClass)) {
            return (T) instances.get(interfaceClass);
        }
        T instance = createInstance(interfaceClass);
        instances.put(interfaceClass, instance);
        return instance;
    }

    private <T> T createInstance(Class<T> interfaceClass) {
        Class<?> implementationClass = findImplementation(interfaceClass);
        if (!implementationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Implementation " + implementationClass.getName()
                    + " is not annotated with @Component");
        }
        Constructor<?> constructor;
        try {
            constructor = implementationClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                    "No default constructor for " + implementationClass.getName(), e);
        }
        T instance;
        try {
            instance = (T) constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Cannot create an instance of " + implementationClass.getName(), e);
        }
        injectDependencies(instance);
        return instance;
    }

    private void injectDependencies(Object instance) {
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot set field " + field.getName(), e);
                }
            }
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClass) {
        if (interfaceClass.isInterface()) {
            return interfaceToImplementation.get(interfaceClass);
        }
        return interfaceClass;
    }
}
