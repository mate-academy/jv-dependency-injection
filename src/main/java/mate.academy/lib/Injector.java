package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (!interfaceClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed," +
                    " missing @Component annotation on the class "
                    + interfaceClazz.getName());
        }
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }
        Object instance = createInstance(interfaceClazz);
        instances.put(interfaceClazz, instance);
        return instance;
    }

    private Object createInstance(Class<?> clazz) {
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);

                    Class<?> fieldClass = field.getType();
                    Object fieldInstance = getInstance(fieldClass);
                    field.set(instance, fieldInstance);
                }
            }
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Injection failed for " + clazz.getName(), e);
        }
    }
}

