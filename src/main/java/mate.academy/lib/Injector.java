package mate.academy.lib;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Injector {
    private static final String IMPL_PACKAGE = "mate.academy.service.impl";
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }
        return createInstance(interfaceClazz);
    }

    private Object createInstance(Class<?> clazz) {
        try {
            if (clazz.isInterface()) {
                clazz = findImplementation(clazz);
            }

            if (!clazz.isAnnotationPresent(Component.class)) {
                throw new RuntimeException("Class " + clazz.getName()
                        + " is missing @Component annotation");
            }

            Constructor<?> constructor = clazz.getDeclaredConstructor();
            Object instance = constructor.newInstance();
            injectDependencies(instance);
            instances.put(clazz, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create an instance of class "
                    + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        try {
            String path = IMPL_PACKAGE.replace('.', '/');
            URL resource = ClassLoader.getSystemClassLoader().getResource(path);
            if (resource == null) {
                throw new RuntimeException("Package " + IMPL_PACKAGE + " not found");
            }

            File directory = new File(resource.toURI());
            if (!directory.exists()) {
                throw new RuntimeException("Directory " + directory.getAbsolutePath()
                        + " not found");
            }

            for (File file : Objects.requireNonNull(directory.listFiles())) {
                if (file.getName().endsWith(".class")) {
                    String className = IMPL_PACKAGE + "." + file.getName()
                            .replace(".class", "");
                    Class<?> implClass = Class.forName(className);

                    if (interfaceClazz.isAssignableFrom(implClass)
                            && implClass.isAnnotationPresent(Component.class)) {
                        return implClass;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Implementation for " + interfaceClazz.getName()
                    + " not found", e);
        }
        throw new RuntimeException("No implementation found for " + interfaceClazz.getName());
    }

    private void injectDependencies(Object instance) throws IllegalAccessException {
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Class<?> fieldType = field.getType();
                Object dependency = getInstance(fieldType);
                field.setAccessible(true);
                field.set(instance, dependency);
            }
        }
    }
}
