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
    private static final Injector injector = new Injector();
    private static Map<Class<?>,Object> mapInstances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public static Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzImplementationInstance = null;
        Field[] declaredFields = clazz.getDeclaredFields();
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz.getName() + " cannot be created");
        }
        for (Field field: declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createInstrance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance,fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot initialize field " + field.getName()
                            + " for:" + clazz.getName());
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createInstrance(clazz);
        }
        return clazzImplementationInstance;
    }

    private static Object createInstrance(Class<?> clazz) {
        if (mapInstances.containsKey(clazz)) {
            return mapInstances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            mapInstances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(clazz.getName() + " cannot be created");
        }
    }

    private static Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>,Class<?>> mapImplementations = new HashMap<>();
        mapImplementations.put(ProductService.class, ProductServiceImpl.class);
        mapImplementations.put(ProductParser.class, ProductParserImpl.class);
        mapImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return mapImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
