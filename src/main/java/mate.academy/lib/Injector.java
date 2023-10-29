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
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> interfaceImpl = new HashMap<>();

    public static Injector getInjector() {
        return new Injector();
    }

    public Object getInstance(Class<?> clazz) {
        Object classImplInstance = null;
        Class<?> classImpl = findImpl(clazz);
        Field[] declaredFields = classImpl.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplInstance = createNewInstance(classImpl);
                try {
                    field.setAccessible(true);
                    field.set(classImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class: "
                                               + classImpl.getName() + ". Field: "
                                               + field.getName());
                }

            }
        }
        if (classImplInstance == null) {
            classImplInstance = createNewInstance(classImpl);
        }
        return classImplInstance;
    }

    public Class<?> findImpl(Class<?> clazz) {
        interfaceImpl.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImpl.put(ProductParser.class, ProductParserImpl.class);
        interfaceImpl.put(ProductService.class, ProductServiceImpl.class);

        if (clazz.isInterface()) {
            return interfaceImpl.get(clazz);
        }
        return clazz;
    }

    public Object createNewInstance(Class<?> classImpl) {
        if (instances.containsKey(classImpl)) {
            return instances.get(classImpl);
        }
        if (!classImpl.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't create instance for class: "
                                       + classImpl.getName() + " because there is no "
                                       + Component.class);
        }
        try {
            Constructor<?> constructor = classImpl.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(classImpl, instance);
            return instance;
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException
                 | IllegalAccessException e) {
            throw new RuntimeException("Can't create new instance of " + classImpl.getName());
        }
    }

}
