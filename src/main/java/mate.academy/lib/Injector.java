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
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazzImplementation = findImplementation(interfaceClazz);
        Field[] declaredFields = clazzImplementation.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazzImplementation);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field Clazz "
                            + clazzImplementation.getName() + ". Field " + field.getName());
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazzImplementation);
        }
        return clazzImplementationInstance;
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
            throw new RuntimeException("Cannot create new instance of " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        if (!interfaceClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't create clazz " + interfaceClazz.getName()
                    + " It isn't @Component");
        }
        if (interfaceClazz.isInterface()) {
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
