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
    private static final String INSTANCE_EXCEPTION_MESSAGE = "Can't create new instance: ";
    private static final String INITIALIZE_EXCEPTION_MESSAGE = "Can't initialize field: ";
    private static final String EXCEPTION_MESSAGE = " in the class: ";
    private static final String ANNOTATION_EXCEPTION_MESSAGE =
            ", The Component annotation is currently missing";
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> classImplementations = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(clazz.getCanonicalName()
                    + ANNOTATION_EXCEPTION_MESSAGE);
        }
        Object clazzImplementationInstance = createNewInstance(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(INITIALIZE_EXCEPTION_MESSAGE + field.getName()
                            + EXCEPTION_MESSAGE + clazz.getName(), e);
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        return instances.computeIfAbsent(clazz, key -> {
            try {
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(INSTANCE_EXCEPTION_MESSAGE + clazz.getName(), e);
            }
        });
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (classImplementations.isEmpty()) {
            initializeClassImplementations();
        }
        return classImplementations.get(interfaceClazz);
    }

    private void initializeClassImplementations() {
        classImplementations.putAll(Map.of(
                FileReaderService.class, FileReaderServiceImpl.class,
                ProductParser.class, ProductParserImpl.class,
                ProductService.class, ProductServiceImpl.class
        ));
    }
}
