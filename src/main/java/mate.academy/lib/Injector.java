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
        Object implementationInstanceClazz = null;
        Class<?> clazz = fingImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class isn`t have  annotation - \"@Component\" "
                    + clazz.getName());
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                implementationInstanceClazz = makeNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(implementationInstanceClazz, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t initialize field value. Class : "
                            + clazz.getName() + ".\n Field : " + field.getName());
                }
            }
        }
        if (implementationInstanceClazz == null) {
            implementationInstanceClazz = makeNewInstance(clazz);
        }
        return implementationInstanceClazz;
    }

    private Object makeNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (InvocationTargetException | NoSuchMethodException
                | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Can`t create a new instance of " + clazz.getName());
        }
    }

    private Class<?> fingImplementation(Class<?> clazz) {
        Map<Class<?>, Class<?>> interfaceImpl = new HashMap<>();
        interfaceImpl.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImpl.put(ProductParser.class, ProductParserImpl.class);
        interfaceImpl.put(ProductService.class, ProductServiceImpl.class);
        if (clazz.isInterface()) {
            return interfaceImpl.get(clazz);
        }
        return clazz;
    }
}
