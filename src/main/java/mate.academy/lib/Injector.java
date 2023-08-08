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
    private static final Injector INJECTOR = new Injector();
    private static final Map<Class<?>, Class<?>> INTERFACES_IMPLEMENTATIONS = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);
    private static final Map<Class<?>, Object> INSTANCES = new HashMap<>();

    public static Injector getInjector() {
        return INJECTOR;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class: "
                            + clazz.getName() + ", field: " + field.getName());
                }
            }
        }
        return clazzImplementationInstance == null ? createNewInstance(clazz)
                : clazzImplementationInstance;
    }

    private static Object createNewInstance(Class<?> clazz) {
        if (INSTANCES.containsKey(clazz)) {
            return INSTANCES.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            INSTANCES.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create new instance of class - " + clazz.getName());
        }
    }

    private static Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            interfaceCheck(interfaceClazz);
            interfaceClazz = INTERFACES_IMPLEMENTATIONS.get(interfaceClazz);
        }
        componentCheck(interfaceClazz);
        return interfaceClazz;
    }

    private static void componentCheck(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Unsupported class - " + clazz
                    + ", should be annotated by @Component");
        }
    }

    private static void interfaceCheck(Class<?> clazz) {
        if (!INTERFACES_IMPLEMENTATIONS.containsKey(clazz)) {
            throw new RuntimeException("Unsupported interface - " + clazz.getName());
        }
    }
}

