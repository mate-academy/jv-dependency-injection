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
    private static final Map<Class<?>, Class<?>> INTERFACE_IMPLEMENTATIONS = Map.of(
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class);
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instancesMap = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplmentantion(interfaceClazz);
        Field[] clazzDeclaredFields = interfaceClazz.getDeclaredFields();
        for (Field clazzDeclaredField : clazzDeclaredFields) {
            if (clazzDeclaredField.isAnnotationPresent(Inject.class)) {
                Object instance = getInstance(clazzDeclaredField.getType());
                clazzDeclaredField.setAccessible(true);
                clazzImplementationInstance = createNewInstance(clazz);
                try {
                    clazzDeclaredField.set(clazzImplementationInstance, instance);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Can't initialize field value. Class:"
                            + clazz.getName()
                            + " Field:"
                            + clazzDeclaredField.getName(), e);
                }
            }
            if (!clazz.isAnnotationPresent(Component.class)) {
                throw new RuntimeException("Can't create an instance of class without annotation"
                        + Component.class.getName());
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instancesMap.containsKey(clazz)) {
            return instancesMap.get(clazz);
        }
        try {
            Constructor<?> clazzConstructor = clazz.getConstructor();
            Object instance = clazzConstructor.newInstance();
            instancesMap.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create an instance of:" + clazz.getName(), e);
        }
    }

    private Class<?> findImplmentantion(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return INTERFACE_IMPLEMENTATIONS.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
