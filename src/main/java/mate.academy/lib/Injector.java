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
    private static final Map<Class<?>, Class<?>> classesImplementation = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);
    private static final Map<Class<?>, Object> classesInstances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementationClazz = classesImplementation.get(interfaceClazz);
        if (!implementationClazz.isAnnotationPresent(Component.class)) {
            throw new UnsupportedOperationException("Can't create class,that is not a component: "
                    + implementationClazz.getName());
        }

        Object implementationInstance = createInstance(implementationClazz);

        for (Field field : implementationClazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(implementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't set field " + field.getName()
                            + " of object " + implementationInstance + " to " + fieldInstance, e);
                }
            }

        }
        return implementationInstance;
    }

    private Object createInstance(Class<?> clazz) {
        if (classesInstances.containsKey(clazz)) {
            return classesInstances.get(clazz);
        }
        try {
            Object instance = clazz.getConstructor().newInstance();
            classesInstances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't get constructor of class" + clazz, e);
        }
    }
}
