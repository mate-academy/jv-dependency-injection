package mate.academy.lib;

import java.lang.annotation.Annotation;
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
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplInstance = null;
        Class<?> clazz = getClazz(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object instanceField = getInstance(field.getType());
                clazzImplInstance = createNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplInstance, instanceField);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException("Cannot set " + clazzImplInstance.toString() 
                            + " value for field " + instanceField.toString(), e);
                }
            }
        }
        if (clazzImplInstance == null) {
            clazzImplInstance = createNewInstance(clazz);
        }
        return clazzImplInstance;
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
        } catch (SecurityException | IllegalArgumentException | ReflectiveOperationException e) {
            throw new RuntimeException("Cannot create an instance of " + clazz.getName(), e);
        }
    }

    private Class<?> getClazz(Class<?> interfaceClazz) {
        Class<?> clazz = (interfaceClazz.isInterface()) 
                ? getImplementations(interfaceClazz) : interfaceClazz;
        if (clazz == null) {
            throw new RuntimeException("There is no known implementation for " 
                    + interfaceClazz.getName());
        }
        Annotation[] annotations = clazz.getAnnotations();
        boolean isComponentPresent = false;
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(Component.class)) {
                isComponentPresent = true;
                break;
            }
        }
        if (!isComponentPresent) {
            throw new RuntimeException(clazz.getName() + " does not have '@Component'");
        }
        return clazz;
    }

    private Class<?> getImplementations(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> implementationsMap = new HashMap<>();
        implementationsMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementationsMap.put(ProductParser.class, ProductParserImpl.class);
        implementationsMap.put(ProductService.class, ProductServiceImpl.class);
        return implementationsMap.get(interfaceClazz);
    }
}
