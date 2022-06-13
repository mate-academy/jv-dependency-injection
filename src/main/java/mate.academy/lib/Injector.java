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
    private final Class<? extends Annotation> annotationForInjection = Inject.class;
    private final Class<? extends Annotation> annotationForImplementation = Component.class;
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();

    {
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
    }

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        compareClassAnnotationWithPattern(clazz, annotationForImplementation);
        Field[] declaredFields = clazz.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(annotationForInjection)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialise field value. Class "
                            + clazz.getName() + "Field: " + field.getName());
                }
            }
        }
        return clazzImplementationInstance == null ? createNewInstance(clazz)
                : clazzImplementationInstance;
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
            throw new RuntimeException("Can't create a new instance of " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            if (!interfaceImplementations.containsKey(interfaceClazz)) {
                throw new RuntimeException("Can't find implementation of "
                        + interfaceClazz.getName() + "in map");
            }
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }

    private void compareClassAnnotationWithPattern(Class<?> clazz,
                                                   Class<? extends Annotation> annotation) {
        if (!clazz.isAnnotationPresent(annotation)) {
            throw new RuntimeException(
                    "Injection failed, missing @Component annotation on the class "
                            + clazz.getName());
        }
    }
}
