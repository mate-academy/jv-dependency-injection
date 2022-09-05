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
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findClassImpl(interfaceClazz);
        checkClassAnnotation(clazz);
        Field[] declaredFields = interfaceClazz.getDeclaredFields();
        Object clazzImplInstance = null;
        for (Field field : declaredFields) {
            field.equals(checkClassAnnotation(clazz));
            clazzImplInstance = createNewInstance(clazz);
        }
        if (clazzImplInstance == null) {
            clazzImplInstance = createNewInstance(clazz);
        }
        return clazzImplInstance;
    }

    private Class<?> findClassImpl(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImpl = new HashMap<>();
        interfaceImpl.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImpl.put(ProductParser.class, ProductParserImpl.class);
        interfaceImpl.put(ProductService.class, ProductServiceImpl.class);
        return interfaceImpl.get(interfaceClazz) == null ? interfaceClazz
                : interfaceImpl.get(interfaceClazz);
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Object instance = clazz.getConstructor().newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of class-> " + clazz.getName(), e);
        }
    }

    private Class<?> checkClassAnnotation(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Component.class)) {
            return clazz;
        }
        throw new RuntimeException("Class not provided with @Component annotation "
                + clazz.getName());
    }
}
