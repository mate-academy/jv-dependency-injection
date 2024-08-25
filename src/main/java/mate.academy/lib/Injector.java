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
    private static final Map<Class<?>, Class<?>> clazzInterfaceImplementations;
    private static final Map<Class<?>, Object> clazzInstances = new HashMap<>();

    static {
        clazzInterfaceImplementations = Map.of(
                ProductService.class, ProductServiceImpl.class,
                ProductParser.class, ProductParserImpl.class,
                FileReaderService.class, FileReaderServiceImpl.class
        );
    }

    private Injector() {

    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("The component has to be marked with a @Component"
                    + " annotation to be used with an injector, the current component is not: "
                    + clazz);
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzInstance = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Unable to access the current field "
                            + field);
                }
            }
        }
        if (clazzInstance == null) {
            clazzInstance = createNewInstance(clazz);
        }
        return clazzInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        try {
            if (clazzInstances.containsKey(clazz)) {
                return clazzInstances.get(clazz);
            }
            Constructor<?> constructor = clazz.getConstructor();
            Object clazzInstance = constructor.newInstance();
            clazzInstances.put(clazz, clazzInstance);
            return clazzInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Unable to create a new instance of the current class "
                    + clazz);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return clazzInterfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
