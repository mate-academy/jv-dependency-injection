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
        if (interfaceClazz.isAnnotationPresent(Component.class)) {
            Object clazzImplementationInstance = null;
            Class<?> clazz = findImplementation(interfaceClazz);
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    clazzImplementationInstance = createNewInstance(clazz);

                    field.setAccessible(true);
                    try {
                        field.set(clazzImplementationInstance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Can not initialize field " + field.getName()
                                + ", in Class " + clazz.getName());
                    }
                }
            }
            if (clazzImplementationInstance == null) {
                clazzImplementationInstance = createNewInstance(clazz);
            }
            return clazzImplementationInstance;
        } else {
            throw new RuntimeException("You can not get instance of no Component class "
                    + interfaceClazz.getName());
        }
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cant create a new instance of " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
            interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
            interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
            interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
            return interfaceImplementations.get(interfaceClazz);
        } else {
            return interfaceClazz;
        }
    }
}
