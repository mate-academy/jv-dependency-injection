package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> components = new HashMap<>();

    private Injector() {
        try {
            registerComponents();
            injectDependencies();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing injector", e);
        }
    }

    public static Injector getInjector() {
        return injector;
    }

    private void registerComponents() {
        Class<?>[] classes = {
                ProductServiceImpl.class,
                ProductParserImpl.class,
                FileReaderServiceImpl.class
        };

        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Component.class)) {
                Constructor<?> constructor;
                try {
                    constructor = clazz.getDeclaredConstructor();
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("Could not find constructor for "
                            + clazz.getName(), e);
                }
                Object instance = null;
                try {
                    instance = constructor.newInstance();
                } catch (InstantiationException
                         | IllegalAccessException
                         | InvocationTargetException e) {
                    throw new RuntimeException("Could not instantiate "
                            + clazz.getName(), e);
                }
                components.put(clazz, instance);
                for (Class<?> element : clazz.getInterfaces()) {
                    components.put(element, instance);
                }
            }
        }
    }

    private void injectDependencies() {
        for (Object component : components.values()) {
            Class<?> clazz = component.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    Class<?> fieldType = field.getType();
                    Object dependency = components.get(fieldType);
                    if (dependency == null) {
                        throw new RuntimeException("No component found for: "
                                + fieldType);
                    }
                    try {
                        field.set(component, dependency);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Not allowed to inject "
                                + fieldType);
                    }
                }
            }
        }
    }

    public <T> T getInstance(Class<T> interfaceClazz) {
        Object component = components.get(interfaceClazz);
        if (component == null) {
            throw new RuntimeException("No component found for: " + interfaceClazz);
        }
        return interfaceClazz.cast(component);
    }
}
