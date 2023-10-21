package mate.academy.lib;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
    private static final Map<Class<?>, Object> instances = new HashMap<>();
    private static final Map<Class<?>, Class<?>> implementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementationClazz = findImplementation(interfaceClazz);

        if (!implementationClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class '" + implementationClazz
                    + "' doesn't have '@Component' annotation");
        }

        Object implementationClazzInstance = null;

        Field[] fields = implementationClazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());

                implementationClazzInstance = createInstance(implementationClazz);

                try {
                    field.setAccessible(true);
                    field.set(implementationClazzInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value with name "
                            + field.getName()
                            + " of "
                            + implementationClazz.getName() + " class", e);
                }
            }
        }

        if (implementationClazzInstance == null) {
            implementationClazzInstance = createInstance(implementationClazz);
        }

        return implementationClazzInstance;
    }

    private Object createInstance(Class<?> implementationClazz) {
        if (instances.containsKey(implementationClazz)) {
            return instances.get(implementationClazz);
        }

        try {
            Object instance = implementationClazz.getConstructor().newInstance();
            instances.put(implementationClazz, instance);
            return instance;
        } catch (InstantiationException | IllegalAccessException
                 | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Can't create an instance of "
                    + implementationClazz + " class", e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            return interfaceClazz;
        }

        return implementations.get(interfaceClazz);
    }
}
