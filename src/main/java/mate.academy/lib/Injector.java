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
    private static final Map<Class<?>, Object> instanceMap = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = getClazz(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't create instance "
                    + "of class that is not marked by @Component");
        }
        Object clazzInstance = createNewInstance(clazz);
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(clazzInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field "
                            + field.getName()
                            + " of instance of " + clazz.getName());
                }
            }
        }
        return clazzInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instanceMap.containsKey(clazz)) {
            return instanceMap.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object o = constructor.newInstance();
            instanceMap.put(clazz, o);
            return o;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                 | IllegalAccessException e) {
            throw new RuntimeException("Can't create instance of " + clazz.getName(), e);
        }
    }

    private Class<?> getClazz(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceMap = new HashMap<>();
        interfaceMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceMap.put(ProductParser.class, ProductParserImpl.class);
        interfaceMap.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return interfaceMap.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
