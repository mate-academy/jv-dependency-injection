package mate.academy.storage;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class InstanceStorageImpl implements Storage<Class<?>, Object> {
    private static Map<Class<?>, Object> instances = new HashMap<>();

    @Override
    public Object get(Class<?> key) {
        if (instances.containsKey(key)) {
            return instances.get(key);
        }
        try {
            Constructor<?> constructor = key.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(key, instance);
            return instance;
        } catch (InstantiationException | IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e) {
            throw new RuntimeException("Bad Instance of " + key.getName());
        }
    }

    @Override
    public boolean isPresent(Class<?> key) {
        return instances.containsKey(key);
    }
}
