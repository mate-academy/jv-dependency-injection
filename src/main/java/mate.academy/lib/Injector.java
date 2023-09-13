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
        if (!interfaceClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Injection failed, missing @Component annotation on the class "
                            + interfaceClazz);
        }

        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzImplementationInstance = createNewInstance(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());

                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t initialize field value. "
                            + "Class: " + clazz.getName() + ". Field: " + field.getName());
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can`t create an instance of " + clazz.getName());
        }

    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplMap = new HashMap<>();
        interfaceImplMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplMap.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplMap.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return interfaceImplMap.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
