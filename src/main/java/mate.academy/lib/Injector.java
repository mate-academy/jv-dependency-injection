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
    private static final Injector injector;
    private static final Map<Class<?>, Object> instances;
    private static final Map<Class<?>, Class<?>> interfacesImpl;

    static {
        injector = new Injector();
        instances = new HashMap<>();
        interfacesImpl = new HashMap<>();
        interfacesImpl.put(ProductService.class, ProductServiceImpl.class);
        interfacesImpl.put(ProductParser.class, ProductParserImpl.class);
        interfacesImpl.put(FileReaderService.class, FileReaderServiceImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        if (clazz.isAnnotationPresent(Inject.class)) {
            throw new RuntimeException("Injection failed, missing @Component annotation "
                    + interfaceClazz.getName() + " was not found.");
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't set Class value of "
                            + clazz.getName() + ". Field:" + field.getName() + e);
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private Object createInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of " + clazz.getName() + e);
        }

    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return interfacesImpl.get(interfaceClazz);
        }
        if (interfacesImpl.get(interfaceClazz) == null) {
            throw new RuntimeException("The implementation for the interface"
                    + interfaceClazz.getName() + " was not found.");
        }
        return interfaceClazz;
    }
}
