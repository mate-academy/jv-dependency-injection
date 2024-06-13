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
    private Map<Class<?>, Class<?>> implementationMap = new HashMap<>();
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        checkIfClassIsComponent(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        Object clazzImplementationInstance = null;
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object instance = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, instance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value."
                            + "Class " + clazz.getName() + ". Field: " + field.getName(),e);
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private static void checkIfClassIsComponent(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("class does not have a @component annotation.");
        }
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object o = constructor.newInstance();
            instances.put(clazz, o);
            return o;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of "
                    + clazz.getName(),e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        implementationMap.put(ProductService.class, ProductServiceImpl.class);
        implementationMap.put(ProductParser.class, ProductParserImpl.class);
        implementationMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return implementationMap.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
