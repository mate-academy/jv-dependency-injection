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

    private final Map<Class<?>, Class<?>> interfaceImplementations
            = Map.of(FileReaderService.class, FileReaderServiceImpl.class, ProductParser.class,
            ProductParserImpl.class, ProductService.class, ProductServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClass) {
        Class<?> instanceClass = findImplementation(interfaceClass);
        Field[] declaredFields = instanceClass.getDeclaredFields();
        Object classImplementationInstance = null;
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplementationInstance = createNewInstance(instanceClass);
                try {
                    field.setAccessible(true);
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot initialize field value. "
                            + "Class: " + instanceClass.getName() + ", Field: "
                            + field.getName(), e);
                }
            }
        }
        if (classImplementationInstance == null) {
            classImplementationInstance = createNewInstance(instanceClass);
        }
        return classImplementationInstance;
    }

    private Object createNewInstance(Class<?> instanceClass) {
        if (instances.containsKey(instanceClass)) {
            return instances.get(instanceClass);
        }
        try {
            Constructor<?> constructor = instanceClass.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(instanceClass, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cannot create a new instance of "
                    + instanceClass.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClass) {
        if (interfaceClass.isInterface()) {
            return interfaceImplementations.get(interfaceClass);
        }
        if (!interfaceClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed, "
                    + "missing @Component annotation on the class "
                    + interfaceClass.getName());
        }
        return interfaceClass;
    }
}
