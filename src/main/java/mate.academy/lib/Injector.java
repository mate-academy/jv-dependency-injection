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
            if (instances.containsKey(interfaceClass)) {
                return instances.get(interfaceClass);
            }

            Class<?> implementationClass = interfaceImplementations.get(interfaceClass);
            if (implementationClass == null
                    || !implementationClass.isAnnotationPresent(Component.class)) {
                throw new IllegalArgumentException(
                        "Injection failed, missing @Component annotation on the class "
                                + (implementationClass != null
                                ? implementationClass.getName() : "null")
                );
            }

            Object instance = createInstance(implementationClass);
            instances.put(interfaceClass, instance);
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

    private void injectDependencies(Object instance) throws ReflectiveOperationException {
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                boolean accessible = field.canAccess(instance);
                field.setAccessible(true);
                Object fieldInstance = getInstance(field.getType());
                field.set(instance, fieldInstance);
                field.setAccessible(accessible);
            }
        }
    }
}
