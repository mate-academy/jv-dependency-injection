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
    private static final Map<Class<?>, Class<?>> implementations = new HashMap<>();

    static {
        implementations.put(ProductService.class, ProductServiceImpl.class);
        implementations.put(ProductParser.class, ProductParserImpl.class);
        implementations.put(FileReaderService.class, FileReaderServiceImpl.class);
    }

    private static final Map<Class<?>, Object> instances = new HashMap<>();
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
                            "Can't set field '" + field.getName()
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
        if (instances.containsKey(implementationClass)) {
            return instances.get(implementationClass);
        }
        try {
            Constructor<?> constructor = implementationClass.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(implementationClass, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of class '"
                    + implementationClass.getName() + "'.", e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClass) {
        if (!implementations.containsKey(interfaceClass)) {
            throw new RuntimeException(
                    "Using improper component. Cannot find implementation for interface '"
                            + interfaceClass.getName() + "'.");
        }
        if (interfaceClass.isInterface()) {
            Class<?> implementationClass = implementations.get(interfaceClass);
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
