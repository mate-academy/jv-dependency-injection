package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
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
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClass) {
        Class<?> injectedClass = findClassImplementation(interfaceClass);
        Object classImplementation = createNewInstance(injectedClass);
        for (Field field : classImplementation.getClass().getFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getClass());
                field.setAccessible(true);
                try {
                    field.set(classImplementation, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field "
                            + field.getName() + " for object "
                            + injectedClass.getName(), e);
                }
            }
        }
        return classImplementation;
    }

    private Object createNewInstance(Class<?> injectedClass) {
        if (instances.containsKey(injectedClass)) {
            return instances.get(injectedClass);
        }
        try {
            Constructor<?> constructor = injectedClass.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(injectedClass, instance);
            return instance;
        } catch (NoSuchMethodException
                 | InvocationTargetException
                 | InstantiationException
                 | IllegalAccessException e) {
            throw new RuntimeException("Can't create object of class "
                    + injectedClass.getName(), e);
        }

    }

    private Class<?> findClassImplementation(Class<?> interfaceClass) {
        if (!interfaceClass.isInterface()
                && !Modifier.isAbstract(interfaceClass.getModifiers())) {
            if (interfaceClass.isAnnotationPresent(Component.class)) {
                return interfaceClass;
            }
        } else {
            if (implementations.containsKey(interfaceClass)) {
                return implementations.get(interfaceClass);
            }
        }
        throw new RuntimeException("No valid implementations for the interface "
                + interfaceClass.getName());
    }
}
