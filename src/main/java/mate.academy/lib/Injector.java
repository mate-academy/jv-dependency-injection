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
    private static final Map<Class<?>, Class<?>> classMap = new HashMap<>();
    private Map<Class<?>, Object> instances = new HashMap<>();

    static {
        classMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        classMap.put(ProductParser.class, ProductParserImpl.class);
        classMap.put(ProductService.class, ProductServiceImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implClass = findImplementationClass(interfaceClazz);
        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("'" + implClass.getName() + "'"
                    + " doesn't have annotation '" + Component.class.getName() + "'");
        }
        Object instance = createNewInstance(implClass);
        for (Field field : implClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldValue = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(instance, fieldValue);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize '"
                            + implClass.getSimpleName() + "."
                            + field.getName(), e);
                }
            }
        }
        return instance;
    }

    private Object createNewInstance(Class<?> implClass) {
        if (instances.containsKey(implClass)) {
            return instances.get(implClass);
        }
        try {
            Constructor<?> constructor = implClass.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(implClass, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create '" + implClass.getName() + "' instance", e);
        }
    }

    private Class<?> findImplementationClass(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return classMap.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
