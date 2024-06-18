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

    public Object getInstance(Class<?> interfaceClass) {
        Class<?> currentClass = findImplementation(interfaceClass);
        Object instance = createNewInstance(currentClass);
        initializeDependencies(instance);
        return instance;
    }

    private Class<?> findImplementation(Class<?> interfaceOrComponentClass) {
        if (interfaceOrComponentClass.isInterface()) {
            return findComponentImplementation(interfaceOrComponentClass);
        } else {
            throw new RuntimeException("Only interfaces can be provided to getInstance");
        }
    }

    private Class<?> findComponentImplementation(Class<?> interfaceClass) {
        Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
                FileReaderService.class, FileReaderServiceImpl.class,
                ProductParser.class, ProductParserImpl.class,
                ProductService.class, ProductServiceImpl.class
        );

        Class<?> implementationClass = interfaceImplementations.get(interfaceClass);
        if (implementationClass == null) {
            throw new RuntimeException("No implementation found for interface "
                    + interfaceClass.getName());
        }

        if (!isComponent(implementationClass)) {
            throw new RuntimeException("Implementation class "
                    + implementationClass.getName()
                    + " is not annotated with @Component");
        }

        return implementationClass;
    }

    private boolean isComponent(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class);
    }

    private Object createNewInstance(Class<?> currentClass) {
        if (instances.containsKey(currentClass)) {
            return instances.get(currentClass);
        }

        try {
            Constructor<?> constructor = currentClass.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(currentClass, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of " + currentClass.getName());
        }
    }

    private void initializeDependencies(Object instance) {
        Field[] declaredFields = instance.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                try {
                    Object dependency = getInstance(field.getType());
                    field.set(instance, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't inject dependency into field: "
                            + field.getName(), e);
                }
            }
        }
    }
}
