package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class InjectorCustom {
    private static final InjectorCustom injector = new InjectorCustom();

    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> implementations = new HashMap<>();

    public static InjectorCustom getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());

                clazzImplInstance = createNewInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(clazzImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value " + "at Class: "
                            + clazz.getName() + " Field: " + field.getName(), e);
                }
            }

        }
        if (clazzImplInstance == null) {
            clazzImplInstance = createNewInstance(interfaceClazz);
        }
        return null;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            Class[] clazzArray = interfaceClazz.getPermittedSubclasses();
            for (Class<?> clazz : clazzArray) {


            }
        }
        return interfaceClazz;
    }

    private Object injectAnnotatedFields(Object object, Field[] declaredFields) {

    }
}
