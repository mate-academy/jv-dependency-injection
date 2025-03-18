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
    private Map<Class<?>, Object> instances = new HashMap<>();
    private Map<Class<?>, Class<?>> interfaceImplementations;

    public Injector() {
        this.interfaceImplementations = Map.of(
                ProductService.class, ProductServiceImpl.class,
                FileReaderService.class, FileReaderServiceImpl.class,
                ProductParser.class, ProductParserImpl.class
        );
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (interfaceClazz.isAnnotationPresent(Component.class)) {
            Class clazz = findImplementation(interfaceClazz);
            Object clazzImplementationInstance = null;
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    clazzImplementationInstance = createNewInstance(clazz);
                    try {
                        field.setAccessible(true);
                        field.set(clazzImplementationInstance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Can`t initialize field value, class: "
                                + clazz.getName() + " field: " + field.getName(), e);
                    }
                }
            }
            if (clazzImplementationInstance == null) {
                clazzImplementationInstance = createNewInstance(clazz);
            }
            return clazzImplementationInstance;
        } else {
            throw new RuntimeException("Your interface " + interfaceClazz
                    + " is not marked as @Component");
        }
    }

    private Object createNewInstance(Class<?> clazz) {
        //if we create an object - let`s use it
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        //create new object
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        return interfaceImplementations.getOrDefault(interfaceClazz, interfaceClazz);
    }
}
