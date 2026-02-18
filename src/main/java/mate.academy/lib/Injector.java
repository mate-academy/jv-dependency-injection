package mate.academy.lib;

import java.lang.reflect.Constructor;
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
    private Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> map = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object classImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplementationInstance = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("cannot init "
                            + clazz.getName() + " field: " + field.getName(), e);
                }
            }
        }
        if (classImplementationInstance == null) {
            classImplementationInstance = createNewInstance(clazz);
        }
        return classImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        Object instance = null;
        Constructor<?> constructor = null;
        try {
            constructor = clazz.getConstructor();
            instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException | InvocationTargetException
                 | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("cannot create instance of " + clazz.getName(), e);
        }

    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz != null && interfaceClazz.isInterface()) {
            Class<?> clazz = map.get(interfaceClazz);
            if (clazz == null) {
                throw new RuntimeException("Class is null");
            }
            if (!clazz.isAnnotationPresent(Component.class)) {
                throw new RuntimeException("Class " + clazz.getName()
                        + " is not annotated with @Component");
            }
            return clazz;
        }
        throw new RuntimeException("Class " + interfaceClazz.getName()
                + " is not annotated with @Component");
    }
}
