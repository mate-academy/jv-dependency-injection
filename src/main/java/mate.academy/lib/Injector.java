package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import mate.academy.exception.InjectionException;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private static final Map<Class<?>, Class<?>> implementationsMap = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );
    private static final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new InjectionException(String.format(
                    "Class %s is not marked with '@Component' annotation",
                    clazz.getName()));
        }

        Object clazzInstance = createNewInstance(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(clazzInstance, fieldInstance);
                } catch (IllegalAccessException ex) {
                    throw new InjectionException(String.format(
                            "Can`t initialize a field %s from class %s",
                            field.getName(), clazz.getName()), ex);
                }
            }
        }
        return clazzInstance;
    }

    public Object createNewInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }
        try {
            Object newInstance = interfaceClazz.getConstructor().newInstance();
            instances.put(interfaceClazz, newInstance);
            return newInstance;
        } catch (ReflectiveOperationException ex) {
            throw new InjectionException(
                    String.format("Can`t create an instance of the class %s",
                            interfaceClazz.getName()), ex);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return implementationsMap.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
