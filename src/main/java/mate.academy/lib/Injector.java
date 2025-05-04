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
    private final Map<Class<?>,Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClass) {
        Object classImplementationInstance = null;
        Class<?> classToBeImplemented = findImplementation(interfaceClass);
        if (!(classToBeImplemented.isAnnotationPresent(Component.class))) {
            throw new RuntimeException("Unsupported class was passed "
                    + classToBeImplemented.getName());
        }
        Field[] declaredFields = classToBeImplemented.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplementationInstance = createNewInstance(classToBeImplemented);
                field.setAccessible(true);
                try {
                    field.set(classImplementationInstance,fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. "
                            + "Class: " + classToBeImplemented.getName()
                            + ". Field: " + field.getName());
                }
            }
        }
        if (classImplementationInstance == null) {
            classImplementationInstance = createNewInstance(classToBeImplemented);
        }
        return classImplementationInstance;
    }

    private Object createNewInstance(Class<?> clas) {
        if (instances.containsKey(clas)) {
            return instances.get(clas);
        }
        try {
            Constructor<?> constructor = clas.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clas,instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + clas.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClass) {
        Map<Class<?>,Class<?>> interfaceImplementation = new HashMap<>();
        interfaceImplementation.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementation.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementation.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClass.isInterface()) {
            return interfaceImplementation.get(interfaceClass);
        }
        return interfaceClass;
    }
}
