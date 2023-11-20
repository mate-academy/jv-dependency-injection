package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import mate.academy.service.ComponentAnnotationNotFoundException;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private static Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);

        if (interfaceClazz.isAnnotationPresent(Component.class)) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    clazzImplementationInstance = createNewInstance(clazz);

                    try {
                        field.setAccessible(true);
                        field.set(clazzImplementationInstance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Can't initialize field value. "
                                + "Class "
                                + clazz.getName()
                                + ". Field "
                                + field.getName(), e);
                    }
                }
            }
            if (clazzImplementationInstance == null) {
                clazzImplementationInstance = createNewInstance(clazz);
            }

        } else {
            throw new ComponentAnnotationNotFoundException("CLass"
                    + interfaceClazz
                    + " isn't component. Can't create instance this class");
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        Constructor<?> constructor = null;
        try {
            constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException
                 | InstantiationException
                 | IllegalAccessException
                 | InvocationTargetException e) {
            throw new RuntimeException("Can't create a new instance of "
                    + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementation = new HashMap<>();
        interfaceImplementation.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementation.put(ProductService.class, ProductServiceImpl.class);
        interfaceImplementation.put(FileReaderService.class, FileReaderServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return interfaceImplementation.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
