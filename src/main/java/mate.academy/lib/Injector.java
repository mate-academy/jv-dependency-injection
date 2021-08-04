package mate.academy.lib;

import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();
    private Map<Class<?>, Object> instances = new HashMap<>();
    private Map<Class<?>, Class<?>> implementationMap;

    {
        implementationMap = new HashMap<>();
        implementationMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementationMap.put(ProductParser.class, ProductParserImpl.class);
        implementationMap.put(ProductService.class, ProductServiceImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object classImplementationInstance = createNewInstance(interfaceClazz);
        Class<?> interfaceClazzImpl = getImplementation(interfaceClazz);
        Field[] interfaceClazzFields = interfaceClazzImpl.getDeclaredFields();
        for (Field field : interfaceClazzFields) {
            if (!field.isAnnotationPresent(Inject.class)) {
                continue;
            }
            Object fieldInstance = getInstance(field.getType());
            try {
                field.setAccessible(true);
                field.set(classImplementationInstance, fieldInstance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Can't initialize field " + field.getName() + " in " + classImplementationInstance.getClass());
            }
        }
        return classImplementationInstance;
    }

    private Class<?> getImplementation(Class<?> interfaceClazz) {
        Class<?> implementation = implementationMap.get(interfaceClazz);
        if (!implementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + implementation.getName() + " should be declared as @Component!");
        }
        return implementation;
    }

    private Object createNewInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }
        try {
            Object instance = interfaceClazz.getConstructor().newInstance();
            instances.put(interfaceClazz, instance);
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Can't create new instance of " + interfaceClazz.getName());
        }
    }
}
