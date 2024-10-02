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
        if (interfaceClazz.isAnnotationPresent(Component.class)) {
            Object clazzImplementationInstance = null;
            Class<?> clazz = findImplementation(interfaceClazz);
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    clazzImplementationInstance = createNewInstance(clazz);
                    field.setAccessible(true);
                    try {
                        field.set(clazzImplementationInstance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Can't initialize field value. Class: "
                                + clazz.getName() + ". Field: " + field.getName(), e);
                    }
                }
            }
            if (clazzImplementationInstance == null) {
                clazzImplementationInstance = createNewInstance(clazz);

            }
            return clazzImplementationInstance;

        } else {
            throw new RuntimeException("This instance cannot be created, because class has"
                    + "no annotation '@Component'");
        }
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
        } catch (NoSuchMethodException | InvocationTargetException
                 | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Can't create new instance of: " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> implementationMap = new HashMap<>();

        implementationMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementationMap.put(ProductParser.class, ProductParserImpl.class);
        implementationMap.put(ProductService.class, ProductServiceImpl.class);

        if (interfaceClazz.isInterface()) {
            return implementationMap.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
