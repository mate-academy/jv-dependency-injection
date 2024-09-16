package mate.academy.lib;

import java.lang.reflect.Field;

public class Injector {
    private static final Injector injector = new Injector();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = interfaceClazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {

            }
        }
        return null;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        return null;
    }
}
