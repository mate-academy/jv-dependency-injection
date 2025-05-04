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
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed, missing @Component"
                    + " annotation on the class " + interfaceClazz.getSimpleName());
        }
        Field[] declaredFields = clazz.getDeclaredFields();
        for (var field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                clazzImplementationInstance = initializeInjectedField(field, clazz);
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private Object initializeInjectedField(Field field, Class<?> clazz) {
        Object fieldInstance = getInstance(field.getType());
        Object instance = createNewInstance(clazz);
        field.setAccessible(true);
        try {
            field.set(instance, fieldInstance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cant set field", e);
        }
        return instance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        Object instance;
        try {
            instance = clazz.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cant create a new instance of "
                    + clazz.getSimpleName(), e);
        }
        instances.put(clazz, instance);
        return instance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
