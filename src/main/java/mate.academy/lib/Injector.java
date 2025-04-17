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

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (interfaceClazz == null) {
            throw new RuntimeException("Cannot instantiate null class");
        }
        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzImplementationInstance = instances.get(clazz);
        if (clazzImplementationInstance != null) {
            return clazzImplementationInstance;
        }
        clazzImplementationInstance = createNewInstance(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to initialize field '" + field.getName()
                        + "' in class " + clazz.getName(), e);
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (clazz == null) {
            throw new RuntimeException("Cannot create instance of null class");
        }
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz.getName() + " is not annotated"
            + " with @Component");
        }
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
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
        Map<Class<?>, Class<?>> interfaceImplementation = Map.of(
                ProductService.class, ProductServiceImpl.class,
                ProductParser.class, ProductParserImpl.class,
                FileReaderService.class, FileReaderServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            Class<?> implementationClass = interfaceImplementation.get(interfaceClazz);
            if (implementationClass == null) {
                throw new RuntimeException(
                    "No implementation found for interface: " + interfaceClazz.getName());
            }
            if (!implementationClass.isAnnotationPresent(Component.class)) {
                throw new RuntimeException("Implementation class "
                    + implementationClass.getName() + " is not annotation with @component");
            }
            return interfaceImplementation.get(interfaceClazz);
        }
        if (!interfaceClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + interfaceClazz.getName()
                + " is not annotated with @Component");
        }
        return interfaceClazz;
    }
}
