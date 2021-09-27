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
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object classImplInstance = null;
        Class<?> clazz = getImplementation(interfaceClazz);
        if (clazz == null) {
            throw new RuntimeException("Injection failed, pass Null");
        }
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed,"
                    + " missing @Component annotation on the class " + clazz.getName());
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field tempField : fields) {
            if (tempField.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(tempField.getType());
                classImplInstance = getNewInstance(clazz);
                try {
                    tempField.setAccessible(true);
                    tempField.set(classImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value."
                           + " Class: " + clazz.getName() + ". Field: " + tempField.getName());
                }
            }
        }
        if (classImplInstance == null) {
            classImplInstance = getNewInstance(clazz);
        }
        return classImplInstance;
    }

    private Object getNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException | InstantiationException
                | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Can't create new instance of " + clazz);
        }
    }

    private Class<?> getImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> mapInstance = new HashMap<>();
        mapInstance.put(FileReaderService.class, FileReaderServiceImpl.class);
        mapInstance.put(ProductService.class, ProductServiceImpl.class);
        mapInstance.put(ProductParser.class, ProductParserImpl.class);
        if (interfaceClazz.isInterface()) {
            return mapInstance.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
