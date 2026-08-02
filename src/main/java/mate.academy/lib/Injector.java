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
    private static final Injector INSTANCE = new Injector();
    private final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );
    private final Map<Class<?>, Object> instances = new HashMap<>();

    private Injector() {
    }

    public static Injector getInjector() {
        return INSTANCE;
    }

    public <T> T getInstance(Class<T> interfaceClazz) {
        Class<?> implementationClass = findImplementation(interfaceClazz);
        Object instance = instances.get(implementationClass);
        if (instance == null) {
            instance = createNewInstance(implementationClass);
            instances.put(implementationClass, instance);
        }
        return interfaceClazz.cast(instance);
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            return interfaceClazz;
        }
        Class<?> implementationClass = interfaceImplementations.get(interfaceClazz);
        if (implementationClass == null) {
            throw new RuntimeException("Can't find implementation for "
                    + interfaceClazz.getName());
        }
        return implementationClass;
    }

    private Object createNewInstance(Class<?> implementationClass) {
        if (!implementationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't create instance of "
                    + implementationClass.getName()
                    + ". Class is missing @Component annotation");
        }
        try {
            Object instance = implementationClass.getDeclaredConstructor().newInstance();
            injectFields(instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of "
                    + implementationClass.getName(), e);
        }
    }

    private void injectFields(Object instance) {
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field "
                            + field.getName()
                            + " in class "
                            + instance.getClass().getName(), e);
                }
            }
        }
    }
}
