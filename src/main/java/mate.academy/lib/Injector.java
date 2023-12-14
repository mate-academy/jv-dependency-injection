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
    private static Map<Class<?>, Class<?>> interfaceImplementations;
    private Map<Class<?>, Object> instances = new HashMap<>();

    static {
        interfaceImplementations = Map.of(
        FileReaderService.class, FileReaderServiceImpl.class,
        ProductParser.class, ProductParserImpl.class,
        ProductService.class, ProductServiceImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object implInstance = null;
        Class<?> implClass = findImplementation(interfaceClazz);
        checkComponent(implClass);
        Field[] fields = implClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                implInstance = createNewInstance(implClass);
                try {
                    field.setAccessible(true);
                    field.set(implInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't init field value: " + field.getName()
                        + "With class: " + implClass.getName());
                }
            }
        }
        return implInstance == null ? createNewInstance(implClass) : implInstance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        return interfaceImplementations.get(interfaceClazz);
    }

    private Object createNewInstance(Class<?> implClass) {
        checkComponent(implClass);
        if (instances.containsKey(implClass)) {
            return instances.get(implClass);
        }
        try {
            Constructor<?> constructor = implClass.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(implClass, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of class: " + implClass.getName(), e);
        }
    }

    private void checkComponent(Class<?> implClass) {
        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(implClass.getName() + "don't mark as Component!");
        }
    }
}
