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
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> implementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementationClazz = getComponentClass(interfaceClazz);
        return instances.getOrDefault(implementationClazz, createNewInstance(implementationClazz));
    }

    private Class<?> getComponentClass(Class<?> interfaceClazz) {
        Class<?> clazz = interfaceClazz.isInterface()
                ? implementations.get(interfaceClazz) : interfaceClazz;
        validateComponentClass(interfaceClazz, clazz);
        return implementations.get(interfaceClazz);
    }

    private void validateComponentClass(Class<?> interfaceClazz, Class<?> componentClass) {
        if (!componentClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't get instance of non-component class "
                    + interfaceClazz.getName());
        }
    }

    private Object createNewInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            injectFields(clazz, instance);
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of class "
                    + clazz.getName(), e);
        }
    }

    private void injectFields(Class<?> clazz, Object instance) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                try {
                    Object fieldInstance = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class: "
                            + clazz.getName() + ", field: " + field.getName(), e);
                }
            }
        }
    }
}
