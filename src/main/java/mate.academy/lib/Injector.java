package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
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
        Class<?> clazz = findImplementation(interfaceClazz);

        if (clazz == null) {
            throw new RuntimeException("There is no implementation for class "
                    + interfaceClazz.getName());
        }

        componentAnnotationChecker(clazz);

        Object classImplementationInstance = createNewInstance(clazz);

        List<Field> fields = List.of(clazz.getDeclaredFields());

        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object instance = getInstance(field.getType());

                try {
                    field.setAccessible(true);
                    field.set(classImplementationInstance, instance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field "
                            + field.getName());
                }
            }
        }
        return classImplementationInstance;
    }

    private Class<?> findImplementation(Class<?> interfaceClass) {
        Map<Class<?>, Class<?>> interfaceImplementation = Map.of(
                ProductParser.class,
                ProductParserImpl.class,
                FileReaderService.class,
                FileReaderServiceImpl.class,
                ProductService.class,
                ProductServiceImpl.class
        );
        return interfaceImplementation.get(interfaceClass);
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
            throw new RuntimeException("Can't create new instance of "
                    + clazz.getName());
        }
    }

    private void componentAnnotationChecker(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("There is no '@Component' annotation in class "
                    + clazz.getName());
        }
    }
}
