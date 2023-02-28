package mate.academy.lib;

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
    private Map<Class<?>, Object> createdInstancesMap = new HashMap<>();
    private Map<Class<?>, Class<?>> interfaceImplementationMap = createInterfaceImplementationMap();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> classImplementation = getImpl(interfaceClazz);
        if (!classImplementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed, "
                    + "missing @Component annotation on the class: "
                    + classImplementation.getName());
        }

        Object classInstance = null;
        Field[] declaredFields = classImplementation.getDeclaredFields();

        for (Field field: declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                // create service
                Object serviceInstance = getInstance(field.getType());;
                // create object
                classInstance = getPresentInstanceOrCreateNew(classImplementation);
                // set service as a field
                try {
                    field.setAccessible(true);
                    field.set(classInstance, serviceInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't set field "
                            + field.getName()
                            + " for " + classInstance.getClass().getName() + " instance");
                }
            }
        }

        if (classInstance == null) {
            classInstance = getPresentInstanceOrCreateNew(classImplementation);
        }
        return classInstance;
    }

    private Map<Class<?>, Class<?>> createInterfaceImplementationMap() {
        Map<Class<?>, Class<?>> map = new HashMap<>();
        map.put(FileReaderService.class, FileReaderServiceImpl.class);
        map.put(ProductParser.class, ProductParserImpl.class);
        map.put(ProductService.class, ProductServiceImpl.class);
        return map;
    }

    private Class<?> getImpl(Class<?> clazz) {
        if (clazz.isInterface()) {
            return interfaceImplementationMap.get(clazz);
        }
        return clazz;
    }

    private Object getPresentInstanceOrCreateNew(Class<?> clazz) {
        if (createdInstancesMap.containsKey(clazz)) {
            return createdInstancesMap.get(clazz);
        }
        try {
            Object instance = clazz.getConstructor().newInstance();
            createdInstancesMap.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create new instance of " + clazz.getName(), e);
        }
    }
}
