package mate.academy.lib;

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
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object classImplementationInstance = null;
        Class<?> interfaceClazzImpl = getImplementation(interfaceClazz);
        Field[] interfaceClazzFields = interfaceClazzImpl.getDeclaredFields();
        for (Field field : interfaceClazzFields) {
            if (!field.isAnnotationPresent(Inject.class)) {
                continue;
            }
            Object fieldInstance = getInstance(field.getType());
            classImplementationInstance = createNewInstance(interfaceClazzImpl);
            try {
                field.setAccessible(true);
                field.set(classImplementationInstance, fieldInstance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Can't initialize field " + field.getName()
                        + " in " + classImplementationInstance.getClass());
            }
        }
        if (classImplementationInstance == null) {
            return createNewInstance(interfaceClazzImpl);
        }
        return classImplementationInstance;
    }

    private Class<?> getImplementation(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            return interfaceClazz;
        }
        Map<Class<?>, Class<?>> implementationMap = new HashMap<>();
        implementationMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementationMap.put(ProductParser.class, ProductParserImpl.class);
        implementationMap.put(ProductService.class, ProductServiceImpl.class);
        return implementationMap.get(interfaceClazz);
    }

    private Object createNewInstance(Class<?> interfaceClazz) {
        if (!interfaceClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't create an instance of non-Component class: "
                    + interfaceClazz.getName());
        }
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }
        try {
            Object instance = interfaceClazz.getConstructor().newInstance();
            instances.put(interfaceClazz, instance);
            return instance;
        } catch (InstantiationException | IllegalAccessException
                | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Can't create new instance of "
                    + interfaceClazz.getName());
        }
    }
}
