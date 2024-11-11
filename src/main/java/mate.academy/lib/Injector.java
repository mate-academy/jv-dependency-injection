package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private static final Map<Class<?>, Class<?>> INTERFACE_IMPLEMENTATIONS = Map.of(
            ProductService.class, ProductServiceImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class
    );

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);

        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + interfaceClazz.getName()
                    + " must be annotated with @Component");
        }

        Field[] declaredField = clazz.getDeclaredFields();
        Object clazzImplementationInstance = createNewInstance(clazz);
        for (Field field : declaredField) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = createNewInstance(findImplementation(field.getType()));
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot initialize field value");
                }
            }
        }

        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                 | IllegalAccessException e) {
            throw new RuntimeException("Cannot create a new instance of: " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return INTERFACE_IMPLEMENTATIONS.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
