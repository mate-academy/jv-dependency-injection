package mate.academy.lib;

import java.lang.reflect.Field;

public class Injector {
    private static final Injector injector = new Injector();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementation = findImplementation(interfaceClazz);

        if (!implementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + implementation.getName()
                    + "don`t have @Component !");
        }

        try {
            Object instance = implementation.getDeclaredConstructor().newInstance();

            for (Field filed : implementation.getDeclaredFields()) {
                if (filed.isAnnotationPresent(Inject.class)) {
                    Object dependency = getInstance(filed.getType());
                    filed.setAccessible(true);
                    filed.set(instance, dependency);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Did not to create object of Class!"
                    + implementation.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        String interfaceName = interfaceClazz.getSimpleName();
        String classImplName = "mate.academy.service.impl." + interfaceName + "Impl";
        try {
            return Class.forName(classImplName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Not found implementation for interface "
                    + interfaceClazz.getName(), e);
        }
    }
}
