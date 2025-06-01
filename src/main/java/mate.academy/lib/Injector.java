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
    private Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class
    );
    private Map<Class<?>, Object> componentInstances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        Object clazzComponentInstance = getComponentInstance(clazz);
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object instance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(clazzComponentInstance, instance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Clazz: "
                            + interfaceClazz.getName()
                            + "; Field: " + field.getName());
                }
            }
        }
        if (clazzComponentInstance == null) {
            clazzComponentInstance = getComponentInstance(clazz);
        }
        return clazzComponentInstance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }

    private Object getComponentInstance(Class<?> clazz) {
        if (componentInstances.containsKey(clazz)) {
            return componentInstances.get(clazz);
        }
        Object componentClassInstance;
        if (clazz.isAnnotationPresent(Component.class)) {
            try {
                Constructor<?> constructor = clazz.getConstructor();
                componentClassInstance = constructor.newInstance();
                componentInstances.put(clazz, componentClassInstance);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Can't create a new instance of " + clazz.getName());
            }
        } else {
            throw new RuntimeException("Injection failed, missing @Component "
                    + "annotation on the class: " + clazz);
        }
        return componentClassInstance;
    }
}
