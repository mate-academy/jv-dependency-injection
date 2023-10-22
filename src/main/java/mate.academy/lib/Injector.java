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
    private final Map<Class<?>,Class<?>> interfaceImplementations = Map.of(
            FileReaderService.class,FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                //create a new object of field type
                Object fieldInstance = getInstance(field.getType());
                //create an object of interfaceClazz object
                clazzImplementationInstance = createNewInstance(clazz);
                //set field type object to interfaceClazz object
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance,fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t initialize field value. Class; "
                            + clazz.getName() + ". Field: " + field.getName(),e);
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        //if we create an object  - let`s use it
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        //create a new object
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz,instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can`t create new instance of " + clazz.getName(),e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return interfaceImplementations.get(interfaceClazz);
        }
        if (!interfaceClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class: " + interfaceClazz.getName()
                    + " has not valid Annotation");
        }
        return interfaceClazz;
    }
}
