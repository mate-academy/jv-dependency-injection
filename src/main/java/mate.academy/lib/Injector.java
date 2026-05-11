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
    private static final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazzImplementation = this.findImplementation(interfaceClazz);

        if (clazzImplementation == null) {
            throw new RuntimeException("Wrong input class. Class is not supported. Class name "
                    + interfaceClazz.getName());
        }

        if (!clazzImplementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Wrong input class. "
                    + "Class doesn't have @Component annotation. Class name "
                    + interfaceClazz.getName());
        }

        Object newInstance = this.createInstance(clazzImplementation);
        Field[] fields = clazzImplementation.getDeclaredFields();

        for (Field field: fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                try {
                    Object fieldInstance = this.getInstance(field.getType());

                    field.setAccessible(true);
                    field.set(newInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't set instance to "
                            + interfaceClazz.getName() + ".Filed name "
                            + field.getName(), e);
                }
            }
        }

        return newInstance;
    }

    private Object createInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object newInstance = constructor.newInstance();

            instances.put(clazz, newInstance);

            return newInstance;
        } catch (ReflectiveOperationException  e) {
            throw new RuntimeException("Can't create new instance from class. Class name "
                    + clazz.getName(), e);
        }

    }

    private Class<?> findImplementation(Class<?> clazz) {
        Map<Class<?>, Class<?>> implementations = Map.of(
                FileReaderService.class, FileReaderServiceImpl.class,
                ProductParser.class, ProductParserImpl.class,
                ProductService.class, ProductServiceImpl.class
        );

        return implementations.get(clazz);
    }
}
