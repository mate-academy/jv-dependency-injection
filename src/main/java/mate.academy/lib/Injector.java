package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Map<Class<?>, Class<?>> INTERFACE_IMPLEMENTATIONS = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);
    private static final Injector injector = new Injector();

    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object implementationInstance = null;
        Class<?> implementationClazz = findImplementation(interfaceClazz);
        if (!implementationClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed: missing @Component annotation on class "
                    + implementationClazz.getName());
        }
        Field[] fields = implementationClazz.getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                implementationInstance = createNewInstance(implementationClazz);
                field.setAccessible(true);
                try {
                    field.set(implementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                            "Failed to set the value of the field during injection."
                                    + " Please check if the field is accessible"
                                    + " and compatible with its assigned value.");
                }
            }
        }
        if (implementationInstance == null) {
            implementationInstance = createNewInstance(implementationClazz);
        }
        return implementationInstance;
    }

    private Object createNewInstance(Class<?> clazzImplementation) {
        if (instances.containsKey(clazzImplementation)) {
            return instances.get(clazzImplementation);
        }
        try {
            Constructor<?> constructor = clazzImplementation.getConstructor();
            Object implementationInstance = constructor.newInstance();
            instances.put(clazzImplementation, implementationInstance);
            return implementationInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to instantiate class: "
                    + clazzImplementation.getName()
                    + ". Make sure it has a default constructor and all dependencies are properly "
                    + "configured.");
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return INTERFACE_IMPLEMENTATIONS.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
