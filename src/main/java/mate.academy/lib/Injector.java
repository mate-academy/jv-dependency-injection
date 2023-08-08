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
    private static final Map<Class<?>, Class<?>> INTERFACE_IMPL = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClass) {
        Class<?> implClass = findImplementation(interfaceClass);
        checkComponentAnnotation(implClass);
        Field[] declaredFields = implClass.getDeclaredFields();
        Object classImplInstance = createNewInstance(implClass);

        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(classImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value "
                            + "Class: " + implClass.getName()
                            + ". Field: " + field.getName(), e);
                }
            }
        }

        return classImplInstance;
    }

    private void checkComponentAnnotation(Class<?> implClass) {
        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't get instance of class "
                    + implClass.getName()
                    + ". Class must have '@Component' annotation!!!");
        }
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
            throw new RuntimeException("Can't create a new instance of " + implClass.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClass) {
        return interfaceClass.isInterface() ? INTERFACE_IMPL.get(interfaceClass) : interfaceClass;
    }
}
