package mate.academy.lib;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] fields = clazz.getDeclaredFields();
        Object instance = createObject(clazz);

        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject field: " + field.getName(), e);
                }
            }
        }
        return instance;
    }

    private Class findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> classClassMap = new HashMap<>();
        classClassMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        classClassMap.put(ProductParser.class, ProductParserImpl.class);
        classClassMap.put(ProductService.class, ProductServiceImpl.class);
        for (Map.Entry<Class<?>, Class<?>> entry : classClassMap.entrySet()) {
            if (entry.getKey().equals(interfaceClazz)) {
                return entry.getValue();
            }
        }
        return interfaceClazz;
    }

    private Object createObject(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("This class '" + clazz
                    + "' is not a component and cannot be injected by the injector.");
        }

        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException
                 | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Can`t create an object with class: " + clazz, e);
        }
    }
}
