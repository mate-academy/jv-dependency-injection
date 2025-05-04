package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import mate.academy.strategy.InterfaceImplementationFinderStrategy;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = InterfaceImplementationFinderStrategy.getImplementation(interfaceClazz);
        Field[] fields = clazz.getDeclaredFields();
        Object classImplementationInstance = null;
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplementationInstance = createOrGetInstance(clazz);
                setFieldToObject(field, classImplementationInstance, fieldInstance, clazz);
            }
        }
        if (classImplementationInstance == null) {
            classImplementationInstance = createOrGetInstance(clazz);
        }
        return classImplementationInstance;
    }

    private Object createOrGetInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Object instance = clazz.getConstructor().newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cant create new instance of class " + clazz.getName(), e);
        }
    }

    private void setFieldToObject(Field field, Object classImplementationInstance,
                                  Object fieldInstance, Class<?> clazz) {
        try {
            field.setAccessible(true);
            field.set(classImplementationInstance, fieldInstance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can't initialize field value. Field " + field.getName()
                    + ", Class " + clazz.getName(), e);
        }
    }
}
