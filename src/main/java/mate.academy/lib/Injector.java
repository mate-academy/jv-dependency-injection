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
        Class<?> implementationClass = findImplementation(interfaceClass);
        Field[] classFields = implementationClass.getDeclaredFields();
        for (Field field : classFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplementationInstance = createNewInstance(implementationClass);

                try {
                    field.setAccessible(true);
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                            "Cannot set field '" + field.getName()
                                    + "' in class '" + implementationClass.getName() + "'.", e);
                }
            }
        }
        if (classImplementationInstance == null) {
            classImplementationInstance = createNewInstance(implementationClass);
        }
        return classImplementationInstance;
    }

    private Object createNewInstance(Class<?> implementationClass) {
        if (INSTANCES.containsKey(implementationClass)) {
            return INSTANCES.get(implementationClass);
        }
        try {
            Constructor<?> constructor = implementationClass.getConstructor();
            Object instance = constructor.newInstance();
            INSTANCES.put(implementationClass, instance);
            return instance;
        } catch (NoSuchMethodException | InvocationTargetException
                | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Can't create instance of class '"
                    + implementationClass.getName() + "'.", e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClass) {
        if (!IMPLEMENTATIONS.containsKey(interfaceClass)) {
            throw new RuntimeException(
                    "Using improper component. Cannot find implementation for interface '"
                            + interfaceClass.getName() + "'.");
        }
        if (interfaceClass.isInterface()) {
            Class<?> implementationClass = IMPLEMENTATIONS.get(interfaceClass);
            if (!implementationClass.isAnnotationPresent(Component.class)) {
                throw new RuntimeException(
                        "Using improper component. There is no annotation 'Component' on class '"
                                + implementationClass.getName() + "'.");
            }
            return implementationClass;
        }
        return interfaceClass;
    }
}
