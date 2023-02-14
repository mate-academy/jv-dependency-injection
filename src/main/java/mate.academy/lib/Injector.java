package mate.academy.lib;

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

    private final Map<Class<?>, Object> instances = new HashMap<>();

    private final Map<Class<?>, Class<?>> implementationMap = Map.of(
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance;
        Class<?> clazz = findImplemantation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Annotation Component not found. Class: " + clazz.getName());
        }
        clazzImplementationInstance = createNewInstance(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field: declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cant inizialize. Field: " + field.getName()
                            + ". Class: " + clazz.getName(), e);
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        try {
            Object newInstance = instances.get(clazz);
            if (newInstance == null) {
                newInstance = clazz.getDeclaredConstructor().newInstance();
                instances.put(clazz, newInstance);
            }
            return newInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Unable to create new instance " + clazz.getName(), e);
        }
    }

    private Class<?> findImplemantation(Class<?> interfaceClazz) {
        return implementationMap.get(interfaceClazz) == null
                ? interfaceClazz : implementationMap.get(interfaceClazz);
    }
}
