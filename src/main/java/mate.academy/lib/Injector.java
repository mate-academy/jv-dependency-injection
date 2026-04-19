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
    private static Map<Class<?>, Class<?>> interfaceImplMap = Map.of(
            ProductService.class, ProductServiceImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class);
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImpl(interfaceClazz);
        Object clazzImplInstance = null;

        if (clazz.isAnnotationPresent(Component.class)) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object instance = injector.getInstance(field.getType());
                    clazzImplInstance = createNewInstance(clazz);
                    try {
                        field.setAccessible(true);
                        field.set(clazzImplInstance, instance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Can't initialized field. Class: "
                                + clazz.getName()
                                + ". Field: " + field.getName(), e);
                    }
                }
            }
            if (clazzImplInstance == null) {
                clazzImplInstance = createNewInstance(clazz);
            }
        } else {
            throw new RuntimeException("Class: " + clazz.getName()
                    + " is not annotated with @Component");
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
            throw new RuntimeException("Cannot create instance of " + clazz.getName(), e);
        }
    }

    private Class<?> findImpl(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            if (!interfaceImplMap.containsKey(interfaceClazz)) {
                throw new RuntimeException("Cannot find class: " + interfaceClazz.getName());
            }
            return interfaceImplMap.get(interfaceClazz);
        }

        return interfaceClazz;
    }
}
