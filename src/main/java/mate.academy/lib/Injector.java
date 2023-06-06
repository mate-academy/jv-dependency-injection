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
    private static final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (clazz.isAnnotationPresent(Component.class)) {
            Object clazzImplementationInstance = null;
            Field[] fields = clazz.getDeclaredFields();
            for (Field field: fields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    clazzImplementationInstance = getOrCreateNewInstance(clazz);
                    field.setAccessible(true);
                    try {
                        field.set(clazzImplementationInstance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Cant initialize field value. "
                                                   + "Class " + clazz.getName()
                                                   + ". Field" + field.getName());
                    }
                }
            }
            if (clazzImplementationInstance == null) {
                clazzImplementationInstance = getOrCreateNewInstance(clazz);
            }
            return clazzImplementationInstance;
        }
        throw new RuntimeException("Injection failed, "
                                   + "missing @Component annotation on the class"
                                   + interfaceClazz.getTypeName());
    }

    private Object getOrCreateNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cant create instance of given class" + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            if (interfaceImplementations.get(interfaceClazz) == null) {
                throw new RuntimeException("The implementation for the interface"
                                           + interfaceClazz.getName() + " was not found.");
            }
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
