package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new ConcurrentHashMap<>();

    private Injector() {

    }

    public static Injector getInjector() {
        return injector;
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> clazz) {
        if (instances.containsKey(clazz)) {
            return (T) instances.get(clazz);
        }

        Class<?> implClass = clazz;
        if (clazz.isInterface()) {
            try {
                implClass = Class.forName(clazz.getPackageName()
                        + ".impl."
                        + clazz.getSimpleName() + "Impl");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Can't create instance of "
                        + clazz.getName()
                        + " - missing @Component");
            }
        }

        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't create instance of "
                    + implClass.getName()
                    + " - missing @Component");
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

            instances.put(clazz, instance);
            return (T) instance;
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of " + implClass.getName(), e);
        }
    }
}
