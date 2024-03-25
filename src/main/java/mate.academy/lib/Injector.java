package mate.academy.lib;


import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();

    public static Injector getInjector() {
        return injector;
    }

    private Map<Class<?>, Object> instaces = new HashMap<>();

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzImplementationInstance = null;
        Field[] declaredFields = interfaceClazz.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                // create a new obj
                Object fieldInstance = getInstance(field.getType());
                //create an obj of interfaceClazz (or implementation class )
                clazzImplementationInstance = createNewInstance(clazz);
                //set `field type object` to `interfaceClazz obj`
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialise field value. Class "
                            + clazz.getName() + " Field " + field.getName());
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        // if we created obj let use it
        if (instaces.containsKey(clazz)) {
            return instaces.get(clazz);
        }
        // create a new obj
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instaces.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException
                 | InvocationTargetException
                 | InstantiationException
                 | IllegalAccessException e) {
            throw new RuntimeException("Can't create new instance of " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> findImplementations = new HashMap<>();
        findImplementations.put(ProductService.class, ProductServiceImpl.class);
        findImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        findImplementations.put(ProductParser.class, ProductParserImpl.class);

        if ((interfaceClazz.isInterface())) {
            return findImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
