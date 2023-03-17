package mate.academy.lib;

import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        if (clazz.isAnnotationPresent(Component.class)) {
            for(Field field : interfaceClazz.getDeclaredFields()) {
                if(field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());

                    clazzImplementationInstance = createNewInstance(clazz);
                    try {
                        field.setAccessible(true);
                        field.set(clazzImplementationInstance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Can't initialize field. Class: "
                                + clazz.getName() + ". Field: " + field.getName());
                    }
                }
            }
            if (clazzImplementationInstance == null) {
                clazzImplementationInstance = createNewInstance(clazz);
            }
        } else {
            throw new RuntimeException("Injection failed, class don't have @Component annotation"
                    + ". Class: " + clazz.getName());
        }

        return clazzImplementationInstance;
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
            throw new RuntimeException("Can't create new instance of " + clazz.getName());
        }

    }

    private Class<?> findImplementation(Class<?> interfaceClass) {
        Map<Class<?>, Class<?>> implementations = new HashMap<>();
        implementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementations.put(ProductParser.class, ProductParserImpl.class);
        implementations.put(ProductService.class, ProductServiceImpl.class);
        return interfaceClass.isInterface()
                ? implementations.get(interfaceClass) : interfaceClass;
    }
}
