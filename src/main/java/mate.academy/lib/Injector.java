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
    private final Map<Class<?>, Class<?>> implementations = Map.of(
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClass) {
        Object classImplementation = null;
        Class<?> clazz = findImpl(interfaceClass);
        if (clazz.isAnnotationPresent(Component.class)) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    classImplementation = createNewInstance(clazz);
                    field.setAccessible(true);
                    try {
                        field.set(classImplementation, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Can't initialize field value. "
                                + "Class: " + clazz.getName()
                                + ". Field: " + field.getName() + ".", e);
                    }
                }

            }
            if (classImplementation == null) {
                classImplementation = createNewInstance(clazz);
            }
            return classImplementation;
        }
        throw new RuntimeException("Injection failed, missing @Component annotaion on the class: "
                + clazz.getSimpleName() + ".");
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create new instance of " + clazz.getName() + ".", e);
        }
    }

    private Class<?> findImpl(Class<?> interfaceClass) {
        if (interfaceClass.isInterface()) {
            return implementations.get(interfaceClass);
        }
        return interfaceClass;
    }
}
