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

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object classImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = interfaceClazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplementationInstance = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialise field. Class: "
                            + clazz.getName()
                            + ". Field: "
                            + field.getName());
                }
            }
        }
        if (classImplementationInstance == null) {
            classImplementationInstance = createNewInstance(clazz);
        }
        return classImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        Constructor<?> constructor;
        Object instance;
        try {
            constructor = clazz.getConstructor();
            instance = constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create object of this class: " + clazz.getName());
        }
        return instance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            if (!interfaceImplementations.get(interfaceClazz)
                    .isAnnotationPresent(Component.class)) {
                throw new RuntimeException("Can't create object of this class: "
                        + interfaceClazz.getName());
            }
            return interfaceImplementations.get(interfaceClazz);
        }
        if (!interfaceClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't create object of this class: "
                    + interfaceClazz.getName());
        }
        return interfaceClazz;
    }
}
