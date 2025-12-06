package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();

    private final Map<Class<?>, Object> instancesCache = new HashMap<>();

    private Injector() {

    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> requestedType) {

        if (instancesCache.containsKey(requestedType)) {
            return instancesCache.get(requestedType);
        }

        Class<?> implementationType = requestedType;

        if (requestedType.isInterface()) {
            implementationType = findImplementation(requestedType);
        }

        if (!implementationType.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Class " + implementationType.getName()
                        + " has no annotation @Component");
        }

        try {
            Constructor<?> constructor = implementationType.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object instance = constructor.newInstance();

            instancesCache.put(requestedType, instance);
            instancesCache.put(implementationType, instance);

            Field[] declaredFields = implementationType.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Class<?> fieldType = field.getType();
                    Object dependency = getInstance(fieldType);

                    field.setAccessible(true);
                    field.set(instance, dependency);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Instance of " + implementationType.getName() + " cannot be created" + e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        String interfacePackage = interfaceClazz.getPackageName();
        String implPackage = interfacePackage + ".impl";
        String implClassName = implPackage + "." + interfaceClazz.getSimpleName() + "Impl";

        try {
            return Class.forName(implClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "Cannot find implementation for " + interfaceClazz.getName(), e);
        }
    }
}
