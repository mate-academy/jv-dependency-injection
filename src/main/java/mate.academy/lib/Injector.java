package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

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
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new ComponentNotFoundException(clazz.getName());
        }
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            injectFields(instance);
            instances.put(clazz, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create an instance of class: "
                    + clazz.getName(), e);
        }
    }

    private void injectFields(Object instance) {
        Class<?> clazz = instance.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                Object fieldInstance = getInstance(fieldType);
                try {
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject field: " + field.getName(), e);
                }
            }
        }
    }
}
