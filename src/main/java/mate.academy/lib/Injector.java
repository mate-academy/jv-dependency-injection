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
    private final Map<Class<?>, Object> implementationInstances = new HashMap<>();
    private final Map<Class<?>, Class<?>> implementations = new HashMap<>();

    {
        implementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementations.put(ProductParser.class, ProductParserImpl.class);
        implementations.put(ProductService.class, ProductServiceImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object implementationInstance;
        Class<?> clazz = findImpl(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Unsupported class: " + clazz.getName());
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                implementationInstance = createInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(implementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field " + field.getName()
                            + " of class " + clazz.getName());
                }
            }
        }
        implementationInstance = createInstance(clazz);
        return implementationInstance;
    }

    private Class<?> findImpl(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return implementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }

    private Object createInstance(Class<?> clazz) {
        if (implementationInstances.containsKey(clazz)) {
            return implementationInstances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            implementationInstances.put(clazz, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName());
        }
    }
}
