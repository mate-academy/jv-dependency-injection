package mate.academy.lib;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object classImplementationInstance = createNewInstance(interfaceClazz);
        return null;
    }

    private Object createNewInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }
        try {
            Object instance = interfaceClazz.getConstructor().newInstance();
            instances.put(interfaceClazz, instance);
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Can't create new instance of " + interfaceClazz.getName());
        }
    }
}
