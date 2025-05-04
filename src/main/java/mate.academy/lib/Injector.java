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
    private static final Map<Class<?>, Object> instanceMap = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = getClazz(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Instance is not marked as @Component: " + clazz.getName());
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
                            + field.getName() + " in " + clazz.getName());
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
            Object object = constructor.newInstance();
            instanceMap.put(clazz, object);
            return object;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of "
                    + clazz.getName(), e);
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
