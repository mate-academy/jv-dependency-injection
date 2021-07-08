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

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementationOf(interfaceClazz);
        Object classImplementationInstance = newInstanceOf(clazz);
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot set a value:" + classImplementationInstance
                                    + " to field: "
                            + fieldInstance);
                }
            }
        }
        return classImplementationInstance;
    }

    private Object newInstanceOf(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Cannot create instance of " + clazz.getName()
                    + ",there is no @Component annotation");
        }
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Object instance = clazz.getConstructor().newInstance();
            instances.put(clazz,instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cannot create instance of " + clazz.getName());
        }
    }

    private Class<?> findImplementationOf(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> implementationsOfInterfaces = new HashMap<>();
        implementationsOfInterfaces.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementationsOfInterfaces.put(ProductParser.class, ProductParserImpl.class);
        implementationsOfInterfaces.put(ProductService.class, ProductServiceImpl.class);
        return implementationsOfInterfaces.get(interfaceClazz);
    }
}
