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
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz.getName()
                    + " is not annotated with @Component");
        }

        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            instances.put(clazz, instance);

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(instance, fieldInstance);
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of "
                    + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            return interfaceClazz;
        }

        try {
            String packageName = interfaceClazz.getPackage().getName();
            String interfaceName = interfaceClazz.getSimpleName();
            String implementationClassName = packageName + ".impl."
                    + interfaceName + "Impl";
            return Class.forName(implementationClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No implementation found for "
                    + interfaceClazz.getName(), e);
        }
    }
}
