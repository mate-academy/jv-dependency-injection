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
    private final Map<Class<?>, Class<?>> interfaceImplementation = Map.of(
            ProductService.class, ProductServiceImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class
    );

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);

        if (clazz.isAnnotationPresent(Component.class)) {
            Object clazzImplementationInstance = instances.get(clazz);

            if (clazzImplementationInstance == null) {
                clazzImplementationInstance = createNewInstance(clazz);
                instances.put(clazz, clazzImplementationInstance);
            }

            injectDependencies(clazz, clazzImplementationInstance);
            return clazzImplementationInstance;
        } else {
            throw new RuntimeException("Injection failed, "
                    + "missing @Component annotation on the class "
                    + clazz.getName());
        }
    }

    private void injectDependencies(Class<?> clazz, Object instance) {
        Field[] declaredFields = clazz.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());

                try {
                    field.setAccessible(true);
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't set field " + field.getName() + " of class "
                            + clazz.getName(), e);
                }
            }
        }
    }

    private Object createNewInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of class " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        return interfaceImplementation.getOrDefault(interfaceClazz, interfaceClazz);
    }
}
