package mate.academy.lib;

import java.lang.reflect.Constructor;
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
    private static final Map<Class<?>, Object> instances = new HashMap<>();
    private static final Map<Class<?>, Class<?>> implementations;
    private static final Injector injector = new Injector();

    static {
        implementations = Map.of(FileReaderService.class, FileReaderServiceImpl.class,
                                    ProductParser.class, ProductParserImpl.class,
                                    ProductService.class, ProductServiceImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = implementations.get(interfaceClazz);

        if (clazz != null) {
            Object clazzImplementationInstance = createInstance(clazz);
            Field[] clazzFields = clazz.getDeclaredFields();

            for (Field field : clazzFields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    boolean isAccessible = field.canAccess(clazzImplementationInstance);

                    field.setAccessible(true);

                    try {
                        field.set(clazzImplementationInstance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Can't inject component to "
                                + clazz.getName() + "." + field.getName(), e);
                    }

                    field.setAccessible(isAccessible);
                }
            }

            return clazzImplementationInstance;
        }

        throw new RuntimeException("Error instantiating class: " + interfaceClazz.getName()
                    + " - component not found");
    }

    private Object createInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't create instance of unannotated class: "
                    + clazz.getName());
        }

        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (InvocationTargetException | NoSuchMethodException
                 | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Error instantiating class: " + clazz.getName(), e);
        }
    }
}
