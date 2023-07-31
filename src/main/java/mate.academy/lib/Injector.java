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
    private static final String INITIALIZE_FIELD_EXCEPTION = "Cannot initialize field value. ";
    private static final String CLASS = "Class: ";
    private static final String FIELD = ", Field: ";
    private static final String CREATE_NEW_INSTANCE_EXCEPTION
            = "Cannot create a new instance of ";
    private static final String MISSING_COMPONENT_EXCEPTION
            = "Injection failed, missing @Component annotation on the class ";
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

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
                    throw new RuntimeException(INITIALIZE_FIELD_EXCEPTION
                            + CLASS + instanceClass.getName() + FIELD + field.getName(), e);
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
            throw new RuntimeException(CREATE_NEW_INSTANCE_EXCEPTION + instanceClass.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClass) {
        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
        Map.of(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClass.isInterface()) {
            return interfaceImplementations.get(interfaceClass);
        }
        if (!interfaceClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(MISSING_COMPONENT_EXCEPTION + interfaceClass.getName());
        }
        return interfaceClass;
    }
}
