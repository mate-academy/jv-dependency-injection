package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private static final Logger LOGGER = Logger.getLogger(Injector.class.getName());
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            ProductService.class, ProductServiceImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class
    );

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClass) {
        try {
            // Check if an instance already exists
            if (instances.containsKey(interfaceClass)) {
                return instances.get(interfaceClass);
            }

            // Get the implementation class for the given interface
            Class<?> implementationClass = interfaceImplementations.get(interfaceClass);
            // Ensure the implementation class is annotated with @Component
            if (implementationClass == null
                    || !implementationClass.isAnnotationPresent(Component.class)) {
                throw new IllegalArgumentException(
                        "Injection failed, missing @Component annotation on the class "
                                + (implementationClass != null
                                ? implementationClass.getName() : "null")
                );
            }

            // Create an instance of the implementation class
            Object instance = createInstance(implementationClass);
            // Store the instance in the map
            instances.put(interfaceClass, instance);
            // Inject dependencies into the instance
            injectDependencies(instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            LOGGER.log(Level.SEVERE, "Injection failed for " + interfaceClass.getName()
                    + " due to " + e.getClass().getSimpleName(), e);
            throw new RuntimeException(
                    "Injection failed for " + interfaceClass.getName()
                            + " due to " + e.getClass().getSimpleName(), e
            );
        }
    }

    private Object createInstance(Class<?> clazz) throws ReflectiveOperationException {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                Object[] parameters = new Object[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    parameters[i] = getInstance(parameterTypes[i]);
                }
                return constructor.newInstance(parameters);
            }
        }
        return clazz.getDeclaredConstructor().newInstance();
    }

    // Method to inject dependencies into the fields of the instance
    private void injectDependencies(Object instance) throws ReflectiveOperationException {
        // Get all declared fields of the instance's class
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            // Check if the field is annotated with @Inject
            if (field.isAnnotationPresent(Inject.class)) {
                // Save the current accessibility state of the field
                boolean accessible = field.canAccess(instance);
                // Make the field accessible to inject the dependency
                field.setAccessible(true);
                // Recursively get the instance for the field's type
                Object fieldInstance = getInstance(field.getType());
                // Inject the resolved dependency into the field
                field.set(instance, fieldInstance);
                // Restore the original accessibility state of the field
                field.setAccessible(accessible);
            }
        }
    }
}
