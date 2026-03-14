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
    private static final Map<Class<?>, Class<?>> implementationMap = Map.of(
                        FileReaderService.class, FileReaderServiceImpl.class,
                        ProductParser.class, ProductParserImpl.class,
                        ProductService.class, ProductServiceImpl.class);
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (clazz == null || !clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("No implementation found for "
                    + interfaceClazz.getName());
        }

        Object clazzImplementationInstance = createNewInstance(clazz);

        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                // create an object of a field type
                Object fieldInstance = getInstance(field.getType());
                // create an object of an interfaceClazz or an implementation class
                // clazzImplementationInstance = createNewInstance(clazz);
                // set 'field type object' to 'interfaceClazz object'
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class "
                            + clazz.getName() + ". Field: " + field.getName(), e);
                }
            }
        }

        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }

        return clazzImplementationInstance;
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
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a news instance of "
                    + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            Class<?> implementation = implementationMap.get(interfaceClazz);

            if (implementation == null) {
                throw new RuntimeException("No implementation found for "
                        + interfaceClazz.getName());
            }

            return implementation;
        }

        return interfaceClazz;
    }

}
