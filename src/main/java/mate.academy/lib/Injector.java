package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new ConcurrentHashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }
        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzImplementationInstance = createNewInstance(clazz);
        instances.put(interfaceClazz, clazzImplementationInstance);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class: "
                            + clazz.getName()
                            + ". Field: "
                            + field.getName());
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Class<?> result;
        if (interfaceClazz == ProductService.class) {
            result = ProductServiceImpl.class;
        } else if (interfaceClazz == ProductParser.class) {
            result = ProductParserImpl.class;
        } else if (interfaceClazz == FileReaderService.class) {
            result = FileReaderServiceImpl.class;
        } else {
            throw new RuntimeException("No implementation found for "
                    + interfaceClazz.getName());
        }
        if (!result.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Missing Component annotation");
        }
        return result;
    }

    private Object createNewInstance(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create an instance of class "
                    + clazz.getName(), e);
        }
    }
}
