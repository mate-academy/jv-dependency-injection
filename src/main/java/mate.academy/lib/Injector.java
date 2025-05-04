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
    private static final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class);

    private static final Map<Class<?>, Object> instances = new HashMap<>();

    private static final Injector injector = new Injector();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = interfaceImplementations.get(interfaceClazz);
        if (clazz == null) {
            throw new RuntimeException("Can't find implementation for interface " + interfaceClazz);
        }

        Field[] fields = clazz.getDeclaredFields();
        Object classInstance = createNewInstance(clazz);
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());

                field.setAccessible(true);
                try {
                    field.set(classInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't set value of field " + field.getName()
                            + " at class " + clazz.getName(), e);
                }
            }
        }

        return classInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Field with class " + clazz + " can't be injected"
            + " (annotation @Component missing)");
        }

        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        try {
            Object instance = clazz.getConstructor().newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance for " + clazz.getName(), e);
        }
    }
}
