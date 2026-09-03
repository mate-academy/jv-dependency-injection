package mate.academy.lib;

import java.lang.reflect.Field;

public class Injector {
    private static final Injector INSTANCE = new Injector();

    public static Injector getInjector() {
        return INSTANCE;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        String implementationName = interfaceClazz.getPackageName()
                + ".impl."
                + interfaceClazz.getSimpleName()
                + "Impl";
        try {
            Class<?> implementationClass = Class.forName(implementationName);
            if (!implementationClass.isAnnotationPresent(Component.class)) {
                throw new RuntimeException(
                        "Class " + implementationClass.getName()
                                + " is not marked with @Component"
                );
            }
            Object object = implementationClass
                    .getDeclaredConstructor()
                    .newInstance();

            Field[] fields = implementationClass.getDeclaredFields();

            for (Field field : fields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object dependency = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(object, dependency);
                }
            }
            return object;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Can't create implementation for " + interfaceClazz.getName(), e
            );
        }
    }
}
