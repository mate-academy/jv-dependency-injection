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

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazzImplementation = findImplementation(interfaceClazz);
        Field[] fields = clazzImplementation.getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldTypeObject = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazzImplementation);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldTypeObject);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field: " + field.getName());
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazzImplementation);
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazzImplementation) {
        if (instances.containsKey(clazzImplementation)) {
            return instances.get(clazzImplementation);
        }
        try {
            Constructor<?> constructor = clazzImplementation.getConstructor();
            Object newInstance = constructor.newInstance();
            instances.put(clazzImplementation, newInstance);
            return newInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of class: "
                                               + clazzImplementation.getName());
        }
    }

    private Class<?> findImplementation(Class<?> clazz) {
        Map<Class<?>, Class<?>> implementations =
                Map.of(FileReaderService.class, FileReaderServiceImpl.class,
                       ProductParser.class, ProductParserImpl.class,
                       ProductService.class, ProductServiceImpl.class
                );
        if (clazz.isInterface()) {
            return implementations.get(clazz);
        }
        throw new RuntimeException("Unsupported type class: " + clazz.getName());
    }
}
