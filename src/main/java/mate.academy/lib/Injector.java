package mate.academy.lib;

import java.lang.reflect.Field;

public class Injector {
    private static final Injector injector = new Injector();

    public static Injector getInjector() {

        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementationClazz = findImplementation(interfaceClazz);
        if (!implementationClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class is not component");
        }
        try {
            Object instance = implementationClazz.getDeclaredConstructor().newInstance();

            Field[] fields = implementationClazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Class<?> fieldType = field.getType();
                    Object dependency = getInstance(fieldType);

                    field.setAccessible(true);
                    field.set(instance, dependency);
                }
            }
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of "
                    + implementationClazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        String interfaceName = interfaceClazz.getSimpleName();
        String implementationName = interfaceClazz.getPackageName()
                    + ".impl."
                    + interfaceClazz.getSimpleName()
                    + "Impl";
        try {
            return Class.forName(implementationName);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't find implementation for "
                    + interfaceClazz.getName(), e);

        }
    }
}
