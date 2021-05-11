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
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> inputType) {
        Class<?> inputImplClass = getImplementation(inputType);
        Object interfaceImplementation = null;
        Field[] allFields = inputImplClass.getDeclaredFields();
        for (Field field : allFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                interfaceImplementation = createNewInstance(inputImplClass);

                try {
                    field.setAccessible(true);
                    field.set(interfaceImplementation, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class "
                            + inputType + ". Field " + field + e);
                }
            }
        }
        if (interfaceImplementation == null) {
            interfaceImplementation = createNewInstance(inputImplClass);
        }
        return interfaceImplementation;
    }

    private Class<?> getImplementation(Class<?> inputType) {
        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        if (inputType.isInterface()) {
            return interfaceImplementations.get(inputType);
        }
        return inputType;
    }

    private Object createNewInstance(Class<?> inputImplClass) {
        if (instances.containsKey(inputImplClass)) {
            return instances.get(inputImplClass);
        }
        try {
            if (inputImplClass.isAnnotationPresent(Component.class)) {
                Constructor<?> constructor = inputImplClass.getConstructor();
                Object instance = constructor.newInstance();
                instances.put(inputImplClass, instance);
                return instance;
            }
            throw new RuntimeException(inputImplClass.getName()
                    + " isn't marked with @Component annotation");
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + inputImplClass + e);
        }
    }
}
