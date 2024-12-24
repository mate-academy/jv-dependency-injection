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
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (clazz == null) {
            throw new RuntimeException("No implementation found for " + interfaceClazz.getName());
        }

        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        Object clazzImplementationInstance = createNewInstance(clazz);

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot initialize field value. "
                            + "Class: " + clazz.getName() + ". Field: " + field.getName(), e);
                }
            }
        }

        instances.put(clazz, clazzImplementationInstance);
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot create new instance of " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceMap = new HashMap<>();
        interfaceMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceMap.put(ProductParser.class, ProductParserImpl.class);
        interfaceMap.put(ProductService.class, ProductServiceImpl.class);

        if (interfaceClazz.isAnnotationPresent(Component.class)) {
            return interfaceClazz;
        }

        if (interfaceClazz.isInterface()) {
            return interfaceMap.get(interfaceClazz);
        }

        return null;
    }
}
