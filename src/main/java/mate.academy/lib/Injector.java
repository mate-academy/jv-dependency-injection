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
    private static Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Object newClassInstance = null;
        if (!(clazz.isAnnotationPresent(Component.class))) {
            throw new RuntimeException("This class don't have annotation" + Component.class);
        }
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object newFieldInstance = getInstance(field.getType());
                newClassInstance = createNewClassInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(newClassInstance, newFieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't set the field value: "
                            + newFieldInstance
                            + " to Object: "
                            + newClassInstance, e);
                }
            }
        }
        if (newClassInstance == null) {
            newClassInstance = createNewClassInstance(clazz);
        }
        return newClassInstance;
    }

    private Object createNewClassInstance(Class<?> clazz) {
        if (instances.get(clazz) != null) {
            return instances.get(clazz);
        }
        try {
            return clazz.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of the class: " 
                    + clazz, e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClass) {
        Map<Class<?>, Class<?>> implementationsMap = new HashMap<>();
        implementationsMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementationsMap.put(ProductParser.class, ProductParserImpl.class);
        implementationsMap.put(ProductService.class, ProductServiceImpl.class);
        if (implementationsMap.get(interfaceClass) == null) {
            return interfaceClass;
        }
        return implementationsMap.get(interfaceClass);
    }
}
