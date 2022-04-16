package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import mate.academy.DirectoryScanner;

public class Injector {
    private static final String HARD_CODED_IMPL_PACKAGE = "mate.academy.service.impl";
    private static final String HARD_CODED_IMPL_DIRECTORY
            = "src\\main\\java\\mate.academy\\service\\impl";
    private static final Injector injector = new Injector();
    private static final Map<Class<?>, Class<?>> implementedInterfaces;

    private static final Map<Class<?>, Object> createdObjects = new HashMap<>();

    static {
        implementedInterfaces = new DirectoryScanner()
                .getComponents(HARD_CODED_IMPL_DIRECTORY, HARD_CODED_IMPL_PACKAGE);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> clazzType) {
        throwIfTypeNotSupported(clazzType);

        Class<?> clazzImplementationType = findImplementation(clazzType);
        Object clientObject = createNewInstance(clazzImplementationType);
        Field[] fields = clazzImplementationType.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object serviceForInjection = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(clientObject, serviceForInjection);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class="
                            + clazzImplementationType.getName() + ", Field=" + field.getName(), e);
                }

            }
        }
        return clientObject;

    }

    private void throwIfTypeNotSupported(Class<?> clazzType) {
        if (!clazzType.isInterface() && !clazzType.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Not supported class=" + clazzType.getName());
        }
    }

    private Object createNewInstance(Class<?> clazz) {
        if (createdObjects.containsKey(clazz)) {
            return createdObjects.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            createdObjects.put(clazz, instance);
            return instance;
        } catch (InstantiationException | InvocationTargetException
                | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("Can't create an object of " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> clazz) {
        if (clazz.isInterface()) {
            return implementedInterfaces.get(clazz);
        }
        return clazz;
    }
}
