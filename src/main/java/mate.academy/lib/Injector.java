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
    private final Map<Class<?>, Class<?>> implementationsMap = new HashMap<>();

    private Injector() {
        implementationsMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementationsMap.put(ProductService.class, ProductServiceImpl.class);
        implementationsMap.put(ProductParser.class, ProductParserImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {

        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();

        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Cannot inject " + clazz.getName()
                    + " Missing @Component annotation.");
        }

        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class "
                            + clazz.getName() + " Field " + field.getName(), e);
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
        Constructor<?> constructor;
        try {
            constructor = clazz.getConstructor();
            Object object = constructor.newInstance();
            instances.put(clazz, object);
            return object;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create new instance of " + clazz.getName(),e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            if (implementationsMap.get(interfaceClazz) != null) {
                return implementationsMap.get(interfaceClazz);
            }
        }
        return interfaceClazz;
    }
}
