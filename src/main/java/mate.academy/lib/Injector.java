package mate.academy.lib;

import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        try {
            // Check if the class is an interface
            if (clazz.isInterface()) {
                Class<?> implementationClass = findImplementationClass(clazz);
                Object instance = createInstance(implementationClass);
                injectFields(instance);
                instances.put(clazz, instance);
                return instance;
            } else {
                Object instance = createInstance(clazz);
                injectFields(instance);
                instances.put(clazz, instance);
                return instance;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create an instance of class: "
                    + clazz.getName(), e);
        }
    }

    private Class<?> findImplementationClass(Class<?> interfaceClass) {
        // Implement your logic to find the implementation class for the interface
        // This is just a placeholder; you should customize it based on your application's
        if (interfaceClass.equals(FileReaderService.class)) {
            return FileReaderServiceImpl.class;
        } else if (interfaceClass.equals(ProductParser.class)) {
            return ProductParserImpl.class;
        } else if (interfaceClass.equals(ProductService.class)) {
            return ProductServiceImpl.class;
        }
        throw new RuntimeException("No implementation class found for: "
                + interfaceClass.getName());
    }

    private Object createInstance(Class<?> clazz) throws Exception {
        // Find the constructor with the most parameters
        Constructor<?> constructor = null;
        for (Constructor<?> c : clazz.getDeclaredConstructors()) {
            if (constructor == null || c.getParameterCount() > constructor.getParameterCount()) {
                constructor = c;
            }
        }
        if (constructor == null) {
            throw new RuntimeException("No suitable constructor found for class: "
                    + clazz.getName());
        }

        // Create an instance using the constructor
        Object[] params = new Object[constructor.getParameterCount()];
        for (int i = 0; i < constructor.getParameterCount(); i++) {
            params[i] = getInstance(constructor.getParameterTypes()[i]);
        }
        return constructor.newInstance(params);
    }

    private void injectFields(Object instance) throws IllegalAccessException {
        Class<?> clazz = instance.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                Object fieldInstance = getInstance(field.getType());
                field.set(instance, fieldInstance);
            }
        }
    }
}
