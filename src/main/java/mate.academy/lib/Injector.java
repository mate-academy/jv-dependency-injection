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
    private static final Map<Class<?>,Object> INSTANCES = new HashMap<>();
    private static final Map<Class<?>, Class<?>> INTERFACE_IMPLEMENTATION = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class);

    public static Injector getInjector() {
        return INJECTOR;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementation = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz.getName() + "hasn't annotation COMPONENT");
        }

        Field[] declaredField = clazz.getDeclaredFields();
        for (Field field : declaredField) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementation = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplementation,fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cant initialize field value. Class "
                            + clazz.getName() + ". Field " + field.getName());
                }
            }
        }
        if (clazzImplementation == null) {
            clazzImplementation = createNewInstance(clazz);
        }
        return clazzImplementation;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (INSTANCES.containsKey(clazz)) {
            return INSTANCES.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            INSTANCES.put(clazz,instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cant create a new instance of " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return INTERFACE_IMPLEMENTATION.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}

