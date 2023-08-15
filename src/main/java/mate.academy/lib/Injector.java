package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
    private Map<Class<?>, Object> instanses = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);

        Field[] declaredFields = interfaceClazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                // create a new object of field type
                Object fieldInstance = getInstance(field.getType());
                // create an object of interfaceClazz (or implementation class)
                clazzImplementationInstance = createNewInstance(clazz);
                // set 'field type object' to interfaceClazz object'
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value."
                            + "Class; " + clazz.getName() + " Field: " + field.getName());
                }
            }
        }
        if (clazzImplementationInstance == null) {
            if (!clazz.isAnnotationPresent(Component.class)) {
                throw new RuntimeException("there is no annotation @Component for implementation "
                        + clazz.getName());
            }
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    // return on class object implementation
    private Object createNewInstance(Class<?> clazz) {
        if (instanses.containsKey(clazz)) {
            return instanses.get(clazz);
        }
        // create a new object
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instanses.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException | InstantiationException
                | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Can't create a new instance of "
                    + clazz.getName());
        }
    }

    // return by implementation class
    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
