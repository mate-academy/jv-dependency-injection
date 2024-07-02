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
    private static final Injector INJECTOR = new Injector();
    private static final Map<Class<?>, Class<?>> CLASS_IMPLEMENTATIONS = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );
    private final Map<Class<?>, Object> classInstance = new HashMap<>();

    public static Injector getInjector() {
        return INJECTOR;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (!validateClazz(interfaceClazz)) {
            throw new RuntimeException("Unsupported class: " + interfaceClazz.getName());
        }
        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzImplementationInstance = createNewInstance(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        initializeFields(clazzImplementationInstance, declaredFields);
        return clazzImplementationInstance;
    }

    private void initializeFields(Object instance, Field[] fields) {
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldObject = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(instance, fieldObject);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class: "
                            + instance.getClass().getName() + ". Field: " + field.getName(), e);
                }
            }
        }
    }

    private boolean validateClazz(Class<?> interfaceClazz) {
        return CLASS_IMPLEMENTATIONS.containsKey(interfaceClazz);
    }

    private Object createNewInstance(Class<?> clazz) {
        if (classInstance.containsKey(clazz)) {
            return classInstance.get(clazz);
        }
        try {
            Constructor<?> constructors = clazz.getConstructor();
            Object instance = constructors.newInstance();
            classInstance.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create new instance of class: " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz == null) {
            throw new RuntimeException("The class interface can't be null.");
        }
        if (interfaceClazz.isInterface()) {
            return CLASS_IMPLEMENTATIONS.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
