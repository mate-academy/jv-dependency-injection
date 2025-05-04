package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> interfaceImplementations;

    private Injector() {
        interfaceImplementations = new HashMap<>();
        try {
            scanPackageForImplementations("mate.academy.service");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Injector", e);
        }
    }

    public static Injector getInjector() {
        return injector;
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return (T) instances.get(interfaceClazz);
        }

        Class<?> implementationClass = interfaceImplementations.get(interfaceClazz);
        if (implementationClass == null) {
            throw new RuntimeException("No implementation found for interface "
                    + interfaceClazz.getName());
        }

        if (!implementationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Implementation class " + implementationClass.getName()
                    + " must be annotated with @Component");
        }

        try {
            Constructor<?> constructor = implementationClass.getDeclaredConstructor();
            Object instance = constructor.newInstance();

            for (Field field : implementationClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    Object dependency = getInstance(field.getType());
                    field.set(instance, dependency);
                }
            }

            instances.put(interfaceClazz, instance);
            return (T) instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create instance of "
                    + implementationClass.getName(), e);
        }
    }

    private void scanPackageForImplementations(String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> componentClasses = reflections.getTypesAnnotatedWith(Component.class);

        for (Class<?> implementationClass : componentClasses) {
            Class<?>[] interfaces = implementationClass.getInterfaces();
            if (interfaces.length != 1) {
                throw new RuntimeException("Class " + implementationClass.getName()
                        + " should implement exactly one interface.");
            }
            interfaceImplementations.put(interfaces[0], implementationClass);
        }
    }
}
