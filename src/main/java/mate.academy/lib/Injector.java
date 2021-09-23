package mate.academy.lib;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Set<Class<?>>> interfacesImplMap = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field: declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplInstance = createNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't set field of class: " + clazz.getName()
                    + ". Field: " + field.getName());
                }
            }
        }
        if (clazzImplInstance == null) {
            clazzImplInstance = createNewInstance(clazz);
        }
        return clazzImplInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            if (!clazz.isAnnotationPresent(Component.class)) {
                throw new RuntimeException("Class has no annotation @Component:"
                        + " " + clazz.getName());
            }
            Object instance = clazz.getConstructor().newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (InstantiationException | IllegalAccessException
                | InvocationTargetException | NoSuchMethodException e) {

            throw new RuntimeException("Can't create instance of: " + clazz.getName());
        }
    }

    @SuppressWarnings({"unchecked"})
    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            if (interfacesImplMap.containsKey(interfaceClazz)) {
                return interfacesImplMap.get(interfaceClazz).stream()
                        .findFirst()
                        .get();
            }
            Reflections reflections = new Reflections("mate.academy.service");
            Set<Class<?>> classes = reflections.getSubTypesOf((Class<Object>) interfaceClazz);
            interfacesImplMap.put(interfaceClazz, classes);
            return classes.stream()
                    .findFirst()
                    .get();
        }
        return interfaceClazz;
    }
}
