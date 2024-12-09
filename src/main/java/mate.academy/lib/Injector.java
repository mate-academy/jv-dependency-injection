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

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        try {
            return findFieldsCheckAnnotationsSetAccessibility(interfaceClazz);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can't get instance of input class!", e);
        }
    }

    private Object findFieldsCheckAnnotationsSetAccessibility(Class<?> interfaceClazz)
            throws IllegalAccessException {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = findFieldsCheckAnnotationsSetAccessibility(
                        field.getType());
                clazzImplementationInstance = createNewInstance(clazz);
                field.setAccessible(true);
                field.set(clazzImplementationInstance, fieldInstance);
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        Constructor<?> constructor = null;
        try {
            constructor = clazz.getConstructor();
            Object object = constructor.newInstance();
            instances.put(clazz, object);
            return object;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                 | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> classClassMap = createImplementationMap();
        Class<?> implementationClazz = classClassMap.get(interfaceClazz);

        if (implementationClazz == null) {
            throw new RuntimeException("No implementation found for: " + interfaceClazz.getName());
        }

        // Sprawdzenie adnotacji @Component tylko na klasie implementacyjnej
        if (!implementationClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Implementation class " + implementationClazz.getName()
                    + " is not annotated with @Component!");
        }

        return implementationClazz;
    }

    private Map<Class<?>, Class<?>> createImplementationMap() {
        Map<Class<?>, Class<?>> immplementationsMap = new HashMap<>();
        immplementationsMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        immplementationsMap.put(ProductParser.class, ProductParserImpl.class);
        immplementationsMap.put(ProductService.class, ProductServiceImpl.class);
        return immplementationsMap;
    }

    private Class<?> determineImplementation(Class<?> interfaceClazz,
                                             Map<Class<?>, Class<?>> immplementationsMap) {
        if (interfaceClazz.isInterface()) {
            return immplementationsMap.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
