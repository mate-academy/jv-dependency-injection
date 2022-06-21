package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class Injector {
    private static final Injector injector = new Injector();
    private static final Map<Class<?>, Object> instancesMap = new HashMap<>();
    private static final Map<Class<?>, Class<?>> interfaceImplementationsMap = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementationClazz = findImplementationClass(interfaceClazz);
        if (!implementationClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Injection failed, missing @Component annotation on the class "
                            + implementationClazz.getSimpleName());
        }
        Field[] implementationClazzFields = implementationClazz.getDeclaredFields();
        Object implementationClazzInstance = null;
        for (Field implementationClazzField : implementationClazzFields) {
            if (implementationClazzField.isAnnotationPresent(Inject.class)) {
                Object implementationClazzFieldInstance =
                        getInstance(implementationClazzField.getType());
                implementationClazzInstance = createNewInstance(implementationClazz);
                implementationClazzField.setAccessible(true);
                try {
                    implementationClazzField.set(
                            implementationClazzInstance, implementationClazzFieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. "
                            + "Class: " + implementationClazz.getName()
                            + ". Field: " + implementationClazzField.getName(), e);
                }
            }
        }
        if (implementationClazzInstance == null) {
            implementationClazzInstance = createNewInstance(implementationClazz);
        }
        return implementationClazzInstance;
    }

    private Object createNewInstance(Class<?> implementationClazz) {
        if (instancesMap.containsKey(implementationClazz)) {
            return instancesMap.get(implementationClazz);
        }
        try {
            Constructor<?> constructor = implementationClazz.getConstructor();
            Object instance = constructor.newInstance();
            instancesMap.put(implementationClazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Can't create a new instance of " + implementationClazz.getName(), e);
        }
    }

    private Class<?> findImplementationClass(Class<?> interfaceClazz) {
        if (interfaceImplementationsMap.containsKey(interfaceClazz)) {
            return interfaceImplementationsMap.get(interfaceClazz);
        }
        ServiceLoader<?> implementationsLoader = ServiceLoader.load(interfaceClazz);
        for (Object implementation : implementationsLoader) {
            Class<?> implementationClazz = implementation.getClass();
            interfaceImplementationsMap.put(interfaceClazz, implementationClazz);
            return implementationClazz;
        }
        return interfaceClazz;
    }
}
