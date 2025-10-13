package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instanceCache = new HashMap<>();

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (instanceCache.containsKey(interfaceClazz)) {
            return instanceCache.get(interfaceClazz);
        }

        Class<?> implClass = findImplementation(interfaceClazz);

        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Implementation " + implClass.getName()
                    + " is not annotated with @Component");
        }

        try {
            Constructor<?> constructor = implClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object instance = constructor.newInstance();

            for (Field field : implClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object dependency = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(instance, dependency);
                }
            }

            instanceCache.put(interfaceClazz, instance);
            return instance;

        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of class: " + implClass.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        String implClassName = interfaceClazz.getPackageName() + ".impl."
                + interfaceClazz.getSimpleName() + "Impl";
        try {
            return Class.forName(implClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't find implementation for: "
                    + interfaceClazz.getName(), e);
        }
    }
}
