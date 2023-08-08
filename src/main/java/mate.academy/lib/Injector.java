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
    private static final Map<Class<?>, Class<?>> interfaceImplementation =
            Map.of(FileReaderService.class, FileReaderServiceImpl.class,
                    ProductParser.class, ProductParserImpl.class,
                    ProductService.class, ProductServiceImpl.class);
    private static final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object classImplementationInstance = null;
        Class<?> classImpl = findImplementationClass(interfaceClazz);
        if (!classImpl.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't initialize object without Component annotation");
        }
        Field[] declaredFields = interfaceClazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplementationInstance = createNewInstance(classImpl);
                try {
                    field.setAccessible(true);
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. "
                            + "Class: " + classImpl.getName() + "Field: " + field.getName());
                }
            }
        }
        return classImplementationInstance == null
                ? createNewInstance(classImpl) : classImplementationInstance;
    }

    private Object createNewInstance(Class<?> classImpl) {
        if (instances.containsKey(classImpl)) {
            return instances.get(classImpl);
        }
        try {
            Constructor<?> constructor;
            constructor = classImpl.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(classImpl, instance);
            return instance;
        } catch (NoSuchMethodException | InvocationTargetException
                 | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Can't create a new instance of " + classImpl.getName());
        }
    }

    private Class<?> findImplementationClass(Class<?> interfaceClazz) {
        return interfaceClazz.isInterface()
                ? interfaceImplementation.get(interfaceClazz) : interfaceClazz;
    }
}
