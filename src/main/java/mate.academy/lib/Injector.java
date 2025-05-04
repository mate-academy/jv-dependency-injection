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
    private static final String MISSING_COMPONENT_MESSAGE = "Injection failed, "
                    + "missing @Component annotation on the class ";
    private static final String CANT_CREATE_INSTANCE_MESSAGE = "Can't create instance of class ";
    private static final String INITIALIZING_EXCEPTION_MESSAGE = "Can't initialize field value. ";
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class
    );

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object classImplementationInstance = null;
        Class<?> currentClass = getClassImplementation(interfaceClazz);
        Field[] classFields = currentClass.getDeclaredFields();
        for (Field field : classFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplementationInstance = createNewInstance(currentClass);
                field.setAccessible(true);
                try {
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(INITIALIZING_EXCEPTION_MESSAGE + field.getName());
                }
            }
        }
        if (classImplementationInstance == null) {
            classImplementationInstance = createNewInstance(currentClass);
        }
        return classImplementationInstance;
    }

    private Object createNewInstance(Class<?> currentClass) {
        if (instances.containsKey(currentClass)) {
            return instances.get(currentClass);
        }
        if (!currentClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(MISSING_COMPONENT_MESSAGE + currentClass.getSimpleName());
        }
        Constructor<?> constructor;
        try {
            constructor = currentClass.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(currentClass, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(CANT_CREATE_INSTANCE_MESSAGE + currentClass.getSimpleName());
        }
    }

    private Class<?> getClassImplementation(Class<?> interfaceClazz) {
        return interfaceImplementations.get(interfaceClazz);
    }
}
