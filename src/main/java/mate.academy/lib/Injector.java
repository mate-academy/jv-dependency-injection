package mate.academy.lib;

import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Map<Class<?>, Class<?>> IMPLEMENTATIONS = new HashMap<>();
    static {
        IMPLEMENTATIONS.put(ProductService.class, ProductServiceImpl.class);
        IMPLEMENTATIONS.put(ProductParser.class, ProductParserImpl.class);
        IMPLEMENTATIONS.put(FileReaderService.class, FileReaderServiceImpl.class);
    }
    private static final Map<Class<?>, Object> INSTANCES = new HashMap<>();
    private static final Injector injector = new Injector();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClass) {
        Object classImplementationInstance = null;
        Class<?> aClass = IMPLEMENTATIONS.get(interfaceClass);
        Field[] classFields = aClass.getDeclaredFields();
        for (Field field : classFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplementationInstance = createNewInstance(aClass);

                try {
                    field.setAccessible(true);
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                            "Cannot set field '" + field.getName()
                                    + "' in class '" + aClass.getName() + "'.", e);
                }
            }
        }
        if (classImplementationInstance == null) {
            classImplementationInstance = createNewInstance(aClass);
        }
        return classImplementationInstance;
    }

    private Object createNewInstance(Class<?> aClass) {
        if (INSTANCES.containsKey(aClass)) {
            return INSTANCES.get(aClass);
        }
        try {
            Constructor<?> constructor = aClass.getConstructor();
            Object instance = constructor.newInstance();
            INSTANCES.put(aClass, instance);
            return instance;
        } catch (NoSuchMethodException | InvocationTargetException
                | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Can't create instance of class '" + aClass.getName() + "'.", e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClass) {
        if (interfaceClass.isInterface() && IMPLEMENTATIONS.containsKey(interfaceClass)) {
            Class<?> aClass = IMPLEMENTATIONS.get(interfaceClass);
            if (!interfaceClass.isAnnotationPresent(Component.class)) {
                throw new RuntimeException(
                        "Using improper class. There is no annotation 'Component' on class '"
                                + interfaceClass.getName() + "'.");
            }
            return aClass;
        }
        return interfaceClass;
    }
}
