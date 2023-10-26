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
    private static final Map<Class<?>, Class<?>> implementaionMap = new HashMap<>();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        initializationMap(implementaionMap);
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementationClazz = findImplementation(interfaceClazz);
        if (!implementationClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed,"
                    + " missing @Component annotation on the class "
                    + implementationClazz.getName());
        }
        Object clazzImplementatiomInstance = createNewInstance(implementationClazz);
        Field [] declaredField = implementationClazz.getDeclaredFields();
        for (Field field : declaredField) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementatiomInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class:"
                            + implementationClazz.getName());
                }
            }
        }
        return clazzImplementatiomInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object clazzInstance = constructor.newInstance();
            instances.put(clazz, clazzInstance);
            return clazzInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return implementaionMap.get(interfaceClazz);
        }
        return interfaceClazz;
    }

    private static void initializationMap (Map<Class<?>, Class<?>> map) {
        map.put(FileReaderService.class, FileReaderServiceImpl.class);
        map.put(ProductParser.class, ProductParserImpl.class);
        map.put(ProductService.class, ProductServiceImpl.class);
    }
}
