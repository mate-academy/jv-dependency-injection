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
    private static final Map<Class<?>, Class<?>> INTERFACE_IMPL = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );
    private static final Map<Class<?>, Object> INSTANCES = new HashMap<>();
    private static final Injector INJECTOR = new Injector();

    public static Injector getInjector() {
        return INJECTOR;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);

        Object clazzImplInstance = createNewInstance(clazz);

        Field[] declaredFields = clazz.getDeclaredFields();

        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(declaredField.getType());
                clazzImplInstance = createNewInstance(clazz);

                declaredField.setAccessible(true);
                try {
                    declaredField.set(clazzImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t initialize field: "
                            + "Class: " + clazz.getName()
                            + " Filed: " + declaredField.getName(), e);
                }
            }
        }

        return clazzImplInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (INSTANCES.containsKey(clazz)) {
            return INSTANCES.get(clazz);
        }

        try {
            Constructor<?> constructors = clazz.getConstructor();
            Object instance = constructors.newInstance();
            INSTANCES.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create new instance: " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz == null) {
            throw new RuntimeException("Class is null");
        }

        Class<?> clazz = interfaceClazz.isInterface()
                ? INTERFACE_IMPL.get(interfaceClazz) : interfaceClazz;

        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Unsupported class " + clazz.getName());
        }

        return clazz;
    }
}
