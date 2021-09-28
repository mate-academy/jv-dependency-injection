package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();

    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClass) {
        Class<?> classImplementation = ImplementationMap.get(interfaceClass);
        if (!classImplementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed, missing annotation "
                    + "@Component on the class " + classImplementation.getName());
        }
        Field[] declaredFields = classImplementation.getDeclaredFields();
        Object classImplementationInstance = null;
        for (Field field: declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplementationInstance = createNewInstance(classImplementation);
                try {
                    field.setAccessible(true);
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value! "
                    + "Class: " + classImplementation.getName() + ". Field: " + field.getName());
                }
            }
        }
        if (classImplementationInstance == null) {
            classImplementationInstance = createNewInstance(classImplementation);
        }
        return classImplementationInstance;
    }

    private Object createNewInstance(Class<?> implementationClass) {
        if (instances.containsKey(implementationClass)) {
            return instances.get(implementationClass);
        }
        try {
            Constructor<?> constructor = implementationClass.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(implementationClass, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Can't create inctance of class "
                    + implementationClass.getName(), e);
        }
    }
}
