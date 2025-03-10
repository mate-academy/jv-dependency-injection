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

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        try {
            Class<?> implementationClass = findImplementation(interfaceClazz);

            if (!implementationClass.isAnnotationPresent(Component.class)) {
                throw new RuntimeException("Missing @Component annotation on class: "
                        + implementationClass.getName());
            }

            if (instances.containsKey(implementationClass)) {
                return instances.get(implementationClass);
            }

            Object instance = createInstance(implementationClass);

            instances.put(implementationClass, instance);

            injectDependencies(instance, implementationClass);

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create an instance of: "
                    + interfaceClazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            return interfaceClazz;
        }

        Map<Class<?>, Class<?>> implementationMap = new HashMap<>();
        implementationMap.put(ProductParser.class,
                ProductParserImpl.class);
        implementationMap.put(FileReaderService.class,
                FileReaderServiceImpl.class);
        implementationMap.put(ProductService.class,
                ProductServiceImpl.class);

        Class<?> implementation = implementationMap.get(interfaceClazz);
        if (implementation == null) {
            throw new RuntimeException("No implementation found for interface: "
                    + interfaceClazz.getName());
        }
        return implementation;
    }

    private Object createInstance(Class<?> clazz) throws Exception {
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private void injectDependencies(Object instance, Class<?> clazz) throws IllegalAccessException {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Class<?> fieldType = field.getType();
                Object dependency = getInstance(fieldType);
                field.setAccessible(true);
                field.set(instance, dependency);
            }
        }
    }
}
