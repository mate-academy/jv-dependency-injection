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
    private static final Injector INJECTOR = new Injector();
    private final Map<Class<?>, Class<?>> interfaceImpls = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return INJECTOR;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        Object clazzImplInstance = null;
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplInstance = createNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. "
                            + "Class: "
                            + clazz.getName()
                            + ". Field: "
                            + field.getName());
                }
            }
        }
        if (clazzImplInstance == null) {
            clazzImplInstance = createNewInstance(clazz);
        }
        return clazzImplInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()
                && !interfaceImpls.get(interfaceClazz).isAnnotationPresent(Component.class)) {
            throw new RuntimeException("This class has no annotation Component");
        }
        return interfaceClazz.isInterface() ? interfaceImpls.get(interfaceClazz) : interfaceClazz;
    }
}
