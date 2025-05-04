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
    private final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                // initialize - create a new object of field type
                Object fieldInstance = getInstance(field.getType());
                // create an object of interfaceClazz (or implementation class)
                clazzImplementationInstance = createNewInstance(clazz);
                // set 'field type object' to 'interfaceClazz object'
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialise field value. "
                            + "Class: " + clazz.getName()
                            + ". Field: "
                            + field.getName(), e);
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object newInstance = constructor.newInstance();
            instances.put(clazz, newInstance);
            return newInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Class<?> implementationClass = interfaceClazz;
        if (interfaceClazz.isInterface()) {
            implementationClass = interfaceImplementations.get(interfaceClazz);
        }
        if (!implementationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed, missing @Component annotation "
                    + "on the class " + implementationClass.getName());
        }
        return implementationClass;
    }
}
