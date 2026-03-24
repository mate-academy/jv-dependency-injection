package mate.academy.lib;

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
    private static final Map<Class<?>, Class<?>> INTERFACE_IMPLS = Map.of(
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductService.class, ProductServiceImpl.class
    );
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = getImplClass(interfaceClazz);
        if (clazz == null) {
            throw new RuntimeException("Class does not exist. Can't create instance of "
                    + interfaceClazz.getName());
        }

        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz.getName() + " is not a component");
        }

        Object clazzImplementationInstance = createInstance(clazz);

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object instance = injector.getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, instance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't create instance of " + clazz.getName(), e);
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object createInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        try {
            Object instance = clazz.getConstructor().newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of " + clazz.getName(), e);
        }
    }

    private Class<?> getImplClass(Class<?> interfaceClazz) {
        return INTERFACE_IMPLS.get(interfaceClazz);
    }
}
