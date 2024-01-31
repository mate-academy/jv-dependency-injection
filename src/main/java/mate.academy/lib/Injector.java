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
    private final Map<Class<?>, Object> instancesOfClazz = new HashMap<>();
    private Map<Class<?>, Class<?>> interfaceImplementation;

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed"
                    + ", missing @Component annotaion on the class "
                    + clazz.getName());
        }
        Field[] declaredFieldsOfInterfaceClazz = clazz.getDeclaredFields();
        Object classImplementationInstance = null;
        for (Field field : declaredFieldsOfInterfaceClazz) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplementationInstance = createNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class :"
                            + clazz.getName() + ". Field: "
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
        if (instancesOfClazz.containsKey(clazz)) {
            return instancesOfClazz.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instanceOfClazz = constructor.newInstance();
            instancesOfClazz.put(clazz, instanceOfClazz);
            return instanceOfClazz;
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        interfaceImplementation
                = Map.of(
                        ProductService.class, ProductServiceImpl.class,
                        ProductParser.class, ProductParserImpl.class,
                        FileReaderService.class, FileReaderServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return interfaceImplementation.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
