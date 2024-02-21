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
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);

        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    interfaceClazz.getSimpleName()
                            + " cannot be initialized with class: "
                            + clazz.getSimpleName()
                            + ", as the @Component annotation is missing."
            );
        }

        Field[] declaredFields = clazz.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazz);
                field.setAccessible(true);

                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot initialize field. Class: "
                            + clazz.getName()
                            + ". Field: "
                            + field.getName(),
                            e);
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
        Object object;

        try {
            constructor = clazz.getConstructor();
            object = constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cannot create new instance of " + clazz.getName(), e);
        }

        instances.put(clazz, object);
        return object;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> implementationsMap = Map.of(
                ProductService.class, ProductServiceImpl.class,
                ProductParser.class, ProductParserImpl.class,
                FileReaderService.class, FileReaderServiceImpl.class
                );

        if (interfaceClazz.isInterface()) {
            return implementationsMap.get(interfaceClazz);
        }

        return interfaceClazz;
    }
}
