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
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object implementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field: declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                implementationInstance = createNewInstance(clazz);

                try {
                    field.setAccessible(true);
                    field.set(implementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t initialize field value. "
                            + "Class: " + clazz.getName() + " . Field: " + field.getName());
                }
            }
        }
        if (implementationInstance == null) {
            implementationInstance = createNewInstance(clazz);
        }
        return implementationInstance;
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
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can`t create instance of " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {

        Class<?> clazz;
        if (interfaceClazz.isInterface()) {
            clazz = interfaceImplementations.get(interfaceClazz);
            if (clazz == null) {
                throw new RuntimeException("No implementation found for interface: "
                        + interfaceClazz.getName());
            }
        } else {
            clazz = interfaceClazz;
        }

        componentAnnotationIsPresent(clazz);
        return clazz;
    }

    private static boolean componentAnnotationIsPresent(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Component.class)) {
            return true;
        }
        throw new RuntimeException("Sorry!!! This class not marked as a component.... "
                + clazz.getName());
    }
}
