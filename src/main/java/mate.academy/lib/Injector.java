package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();
    private static final String IMPL = "Impl";
    private static final String CLASS = "Class";
    private static final String SPACE = " ";

    private final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            mate.academy.service.ProductService.class,
            mate.academy.service.impl.ProductServiceImpl.class,
            mate.academy.service.FileReaderService.class,
            mate.academy.service.impl.FileReaderServiceImpl.class,
            mate.academy.service.ProductParser.class,
            mate.academy.service.impl.ProductParserImpl.class
    );

    private final Map<Class<?>, Object> instances = new HashMap<>();

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }

        Class<?> implClass;
        try {
            implClass = findImplementation(interfaceClazz);
            if (!implClass.isAnnotationPresent(Component.class)) {
                throw new RuntimeException(CLASS + SPACE + implClass.getName()
                        + " is missing @Component annotation");
            }

            Object implInstance = createInstance(implClass);
            instances.put(interfaceClazz, implInstance);
            return implInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of class: "
                    + interfaceClazz.getName(), e);
        }
    }

    private Object createInstance(Class<?> implClass) throws ReflectiveOperationException {
        Object instance = implClass.getDeclaredConstructor().newInstance();
        for (Field field : implClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                field.setAccessible(true);
                field.set(instance, fieldInstance);
            }
        }
        return instance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceImplementations.containsKey(interfaceClazz)) {
            return interfaceImplementations.get(interfaceClazz);
        }
        throw new RuntimeException("Can't find implementation for: " + interfaceClazz.getName());
    }
}
