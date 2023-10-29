package mate.academy.lib;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();
    private Map<Class<?>, Object> instances = new HashMap<>();

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }

        if (!interfaceClazz.isInterface()) {
            throw new IllegalArgumentException("Only interfaces can be used with Injector");
        }

        Class<?> implementationClazz = findImplementation(interfaceClazz);

        if (implementationClazz == null) {
            throw new IllegalArgumentException("No implementation found for interface "
                    + interfaceClazz.getName());
        }

        try {
            if (instances.containsKey(interfaceClazz)) {
                return instances.get(interfaceClazz);
            }

            Object instance = implementationClazz.getDeclaredConstructor().newInstance();
            injectDependencies(instance);
            instances.put(interfaceClazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Injection failed: " + e.getMessage(), e);
        }
    }

    private <T> void injectDependencies(T instance) throws IllegalAccessException {
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

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        for (Class<?> clazz : getAllClassesInPackage("mate.academy.service.impl")) {
            if (interfaceClazz.isAssignableFrom(clazz)
                    && clazz.isAnnotationPresent(Component.class)) {
                return clazz;
            }
        }
        return null;
    }

    private List<Class<?>> getAllClassesInPackage(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        try {
            String path = packageName.replace(".", "/");
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource(path);
            if (resource != null) {
                File packageDir = new File(resource.getFile());
                if (packageDir.exists()) {
                    for (File file : packageDir.listFiles()) {
                        if (file.isFile() && file.getName().endsWith(".class")) {
                            String className = packageName + "."
                                    + file.getName().substring(0, file.getName().length() - 6);
                            classes.add(Class.forName(className));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while scanning classes: " + e.getMessage(), e);
        }
        return classes;
    }
}
