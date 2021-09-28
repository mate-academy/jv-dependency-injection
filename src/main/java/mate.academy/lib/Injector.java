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
    private static final Map<Class<?>, Class<?>> interfaceAndImplementation = new HashMap<>();
    private final Map<Class<?>, Object> classInstances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        Object clazzImplementationInstance = createNewInstance(clazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Annotation @Component doesn't exist in class: "
                    + clazz.getName());
        }
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. "
                            + "Class: " + clazz.getName() + " Field: " + field.getName());
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (classInstances.containsKey(clazz)) {
            return classInstances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            classInstances.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException | InvocationTargetException
                | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Can't create new instance of " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceOrImplementation) {
        if (!interfaceOrImplementation.isInterface()) {
            interfaceAndImplementation.put(FileReaderService.class, FileReaderServiceImpl.class);
            interfaceAndImplementation.put(ProductService.class, ProductServiceImpl.class);
            interfaceAndImplementation.put(ProductParser.class, ProductParserImpl.class);
            return interfaceOrImplementation;
        }
        return interfaceAndImplementation.get(interfaceOrImplementation);
    }
}
