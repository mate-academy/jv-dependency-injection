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
    private final Map<Class<?>, Class<?>> interfaceImplementations =
            Map.of(FileReaderService.class, FileReaderServiceImpl.class,
                    ProductParser.class, ProductParserImpl.class,
                    ProductService.class, ProductServiceImpl.class
            );
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClass) {
        Object classImplementationInstance = null;
        Class<?> classImplementation = findImplementation(interfaceClass);
        Field[] declaredFields = classImplementation.getDeclaredFields();
        if (!classImplementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed, "
                    + "missing @Component annotation on the class " + classImplementation);
        }
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                if (instances.containsKey(classImplementation)) {
                    return instances.get(classImplementation);
                }
                classImplementationInstance = createNewInstance(classImplementation);
                try {
                    field.setAccessible(true);
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. "
                            + "Class: " + classImplementation.getName()
                            + ". Field: " + field.getName());
                }
            }
        }
        if (classImplementationInstance == null) {
            if (instances.containsKey(classImplementation)) {
                return instances.get(classImplementation);
            }
            classImplementationInstance = createNewInstance(classImplementation);
        }
        return classImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        Constructor<?> constructor;
        try {
            constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
