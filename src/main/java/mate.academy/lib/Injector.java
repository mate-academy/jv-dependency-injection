package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
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
    private static final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = 0;
        Class<?> clazz = findClass(interfaceClazz);
        Field[] fields = clazz.getDeclaredFields();
        clazzImplementationInstance = createNewInstance(clazz);
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't set object to field: "
                            + field.getName());
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }
        try {
            Constructor<?> constructor = interfaceClazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(interfaceClazz, instance);
            return instance;
        } catch (NoSuchMethodException | InstantiationException
                 | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Can't create instance: "
                    + interfaceClazz.getName());
        }
    }

    private Class<?> findClass(Class<?> clazz) {

        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        if (clazz.isInterface()) {
            return interfaceImplementations.get(clazz);
        }
        if (Arrays.stream(clazz.getAnnotations()).noneMatch(annotation ->
                annotation.annotationType().equals(Component.class))) {
            throw new RuntimeException("Unsupported class" + clazz.getName());
        }
        return clazz;
    }
}
