package mate.academy.lib;

import java.lang.reflect.Field;
import mate.academy.storage.ImplementationStorageImpl;
import mate.academy.storage.InstanceStorageImpl;
import mate.academy.storage.Storage;

public class Injector {
    private static final Injector injector = new Injector();
    private static Storage<Class<?>, Class<?>> implementations
            = new ImplementationStorageImpl();
    private static Storage<Class<?>, Object> instances
            = new InstanceStorageImpl();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] fields = clazz.getDeclaredFields();
        if (clazz.isAnnotationPresent(Component.class)) {
            for (Field field: fields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    clazzImplementationInstance = createNewInstance(clazz);
                    field.setAccessible(true);
                    try {
                        field.set(clazzImplementationInstance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Can't initialize field value:\n"
                                + "for " + clazz.getName() + "."
                                + field.getName());
                    }
                }
            }
            if (clazzImplementationInstance == null) {
                clazzImplementationInstance = createNewInstance(clazz);
            }
            return clazzImplementationInstance;
        }
        throw new RuntimeException("This class are without @Component annotation\n"
                + clazz.getName());
    }

    private Object createNewInstance(Class<?> clazz) {
        return instances.get(clazz);
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (((ImplementationStorageImpl)implementations)
                .isPresent(interfaceClazz)) {
            return implementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
