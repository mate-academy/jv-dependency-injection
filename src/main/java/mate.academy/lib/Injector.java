package mate.academy.lib;

import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();

    public Injector() {
        // Initialize interface-to-implementation class mappings
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        if (!isSupportedClass(clazz)) {
            throw new RuntimeException("Unsupported class: " + clazz.getName());
        }

        try {
            Class<?> implementationClass = findImplementationClass(clazz);
            if (implementationClass == null) {
                throw new ComponentNotFoundException("No implementation class found for: "
                        + clazz.getName());
            }

            Object instance = createInstance(implementationClass);
            injectFields(instance);
            instances.put(clazz, instance);
            return instance;
        } catch (Exception e) {
            throw new ComponentNotFoundException("Failed to create an instance of class: "
                    + clazz.getName());
        }
    }

    private boolean isSupportedClass(Class<?> clazz) {
        return clazz.isInterface() || isComponentAnnotated(clazz);
    }

    private boolean isComponentAnnotated(Class<?> clazz) {
        return clazz.isAnnotationPresent(Component.class);
    }

    private Class<?> findImplementationClass(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            Class<?> implementationClass = interfaceImplementations.get(interfaceClazz);
            if (implementationClass != null) {
                return implementationClass;
            }
        }
        return interfaceClazz;
    }

    private Object createInstance(Class<?> clazz) throws Exception {
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName(), e);
        }
    }

    private void injectFields(Object instance) throws IllegalAccessException {
        Class<?> clazz = instance.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                try {
                    field.setAccessible(true);
                    Object fieldInstance = getInstance(field.getType());
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
