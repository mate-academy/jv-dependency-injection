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
    private final Map<Class<?>, Class<?>> implementationMap;
    private final Map<Class<?>, Object> instances;

    private Injector() {
        implementationMap = new HashMap<>();
        implementationMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementationMap.put(ProductParser.class, ProductParserImpl.class);
        implementationMap.put(ProductService.class, ProductServiceImpl.class);

        instances = new HashMap<>();
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            throw new RuntimeException("Unsupported class: " + interfaceClazz.getName());
        }

        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }

        Class<?> clazz = implementationMap.get(interfaceClazz);
        if (clazz == null) {
            throw new RuntimeException("No implementation found for interface: "
                    + interfaceClazz.getName());
        }

        Object clazzImplementationInstance = createNewInstance(clazz);
        initializeFields(clazz, clazzImplementationInstance);

        instances.put(interfaceClazz, clazzImplementationInstance);

        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Unable to create a new class instance "
                    + clazz.getName());
        }
    }

    private void initializeFields(Class<?> clazz, Object instance) {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot initialize field value. "
                            + "Class: " + clazz.getName() + ", Field: " + field.getName());
                }
            }
        }
    }
}
