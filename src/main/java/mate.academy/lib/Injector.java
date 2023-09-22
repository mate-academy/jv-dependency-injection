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
    public static final Map<Class<?>, Class<?>> implementationMap = Map.of(
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class
    );
    private static final Injector injector = new Injector();
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object classImplInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        checkClassAnnotation(clazz);
        Field[] fields = clazz.getDeclaredFields();

        for (Field field: fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());

                classImplInstance = createNewInstance(clazz);

                try {
                    field.setAccessible(true);
                    field.set(classImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t initialize field value. "
                            + "Class " + clazz.getName() + "Field " + field.getName());
                }
            }
        }
        if (classImplInstance == null) {
            classImplInstance = createNewInstance(clazz);
        }
        return classImplInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        Constructor<?> constructor = null;
        try {
            constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can`t create a new instance of " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> clazz) {
        if (clazz.isInterface()) {
            return implementationMap.get(clazz);
        }
        return clazz;
    }

    private void checkClassAnnotation(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(clazz.getName() + " - is not annotated by @Component");
        }
    }
}
