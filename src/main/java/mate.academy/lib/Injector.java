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
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }
        Class<?> implementationClass = findImplementation(interfaceClazz);
        Object classImplementationInstance = createInstance(implementationClass);
        Field[] fields = implementationClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Could not set field " + field.getName()
                            + " of class " + interfaceClazz.getName(), e);
                }
            }
        }
        instances.put(interfaceClazz, classImplementationInstance);
        return classImplementationInstance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> implementations = new HashMap<>();
        implementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementations.put(ProductParser.class, ProductParserImpl.class);
        implementations.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            Class<?> implementationClass = implementations.get(interfaceClazz);
            if (implementationClass == null) {
                throw new RuntimeException("No implementation found for interface "
                        + interfaceClazz.getName());
            }
            if (!implementationClass.isAnnotationPresent(Component.class)) {
                throw new RuntimeException("Implementation class "
                        + implementationClass.getName()
                        + " is not annotated with @Component");
            }
            return implementationClass;
        }
        if (interfaceClazz.isAnnotationPresent(Component.class)) {
            return interfaceClazz;
        }
        throw new RuntimeException("Class " + interfaceClazz.getName()
                + " is not a valid component");
    }

    private Object createInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException
                 | InvocationTargetException
                 | InstantiationException
                 | IllegalAccessException e) {
            throw new RuntimeException("Could not instantiate class " + clazz.getName(), e);
        }
    }
}
