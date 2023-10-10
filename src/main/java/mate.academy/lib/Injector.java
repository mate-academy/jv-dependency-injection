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
    private static final Map<Class<?>, Class<?>> interfaceImplementations;

    static {
        interfaceImplementations = Map.of(ProductParser.class, ProductParserImpl.class,
                FileReaderService.class, FileReaderServiceImpl.class,
                ProductService.class, ProductServiceImpl.class);
    }

    private final Map<Class<?>, Object> createdInstances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzImplementationInstance = null;
        final Field[] declaredFields = clazz.getDeclaredFields();
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed, missing @Component annotation on "
                    + "the class " + interfaceClazz.getName());
        }
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                //1. create an object of required field type (this object will be injected).
                final Object fieldInstance = getInstance(field.getType());

                //2. create an object of type interfaceClazz or its implementation class.
                //Injection will be done into this object.
                clazzImplementationInstance = createNewInstance(clazz);

                //3. set (inject) 'inner object' into 'outer object'.
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class:"
                            + clazz.getName() + ". Field: " + field.getName());
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(final Class<?> clazz) {
        //if an object is already created let's use it.
        if (createdInstances.containsKey(clazz)) {
            return createdInstances.get(clazz);
        }
        try {
            final Constructor<?> constructor = clazz.getConstructor();
            final Object instance = constructor.newInstance();
            createdInstances.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                 | IllegalAccessException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName());
        }
    }

    private Class<?> findImplementation(final Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
