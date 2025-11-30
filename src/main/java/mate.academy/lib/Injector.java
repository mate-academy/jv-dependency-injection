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
    private static final Injector injector = new Injector();

    private static final Map<Class<?>, Object> instanceCache = new HashMap<>();

    private static final Map<Class<?>, Class<?>> implementationMap = new HashMap<>();

    static {
        implementationMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementationMap.put(ProductParser.class, ProductParserImpl.class);
        implementationMap.put(ProductService.class, ProductServiceImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public static Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementationClass = getImplementation(interfaceClazz);

        Object cachedInstance = instanceCache.get(implementationClass);
        if (cachedInstance != null) {
            return cachedInstance;
        }

        checkComponentAnnotation(implementationClass);
        Object instance = createInstance(implementationClass);
        injectDependencies(instance);

        instanceCache.put(implementationClass, instance);
        return instance;
    }

    private static Class<?> getImplementation(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return clazz;
        }

        Class<?> implementationClass = implementationMap.get(clazz);
        if (implementationClass == null) {
            throw new RuntimeException("There is no implementation for " + clazz.getName());
        }
        return implementationClass;
    }

    private static void checkComponentAnnotation(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz.getName()
                    + " is not annotated with @Component");
        }
    }

    public static <T> T createInstance(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cannot create instance of " + clazz.getName(), e);
        }
    }

    private static void injectDependencies(Object instance) {
        Class<?> clazz = instance.getClass();
        for (Field field : clazz.getDeclaredFields()) {

            if (field.isAnnotationPresent(Inject.class)) {
                Class<?> dependencyType = field.getType();

                Object dependency = getInstance(dependencyType);

                field.setAccessible(true);
                try {
                    field.set(instance, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot inject into " + field.getName(), e);
                }
            }
        }
    }
}
