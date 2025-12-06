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

    private final Map<Class<?>, Class<?>> interfaceToImpl = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    private final Map<Class<?>, Object> instancesCache = new HashMap<>();

    private Injector() {

    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> requestedType) {

        if (instancesCache.containsKey(requestedType)) {
            return instancesCache.get(requestedType);
        }

        Class<?> implementationType = requestedType;

        if (requestedType.isInterface()) {
            implementationType = findImplementation(requestedType);
        }

        if (instancesCache.containsKey(implementationType)) {
            return instancesCache.get(implementationType);
        }

        if (!implementationType.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Class " + implementationType.getName()
                        + " has no annotation @Component");
        }

        try {
            Constructor<?> constructor = implementationType.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object instance = constructor.newInstance();

            instancesCache.put(requestedType, instance);
            instancesCache.put(implementationType, instance);

            Field[] declaredFields = implementationType.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Class<?> fieldType = field.getType();
                    Object dependency = getInstance(fieldType);

                    field.setAccessible(true);
                    field.set(instance, dependency);
                }
            }
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Failed to create instance of " + implementationType.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Class<?> implClass = interfaceToImpl.get(interfaceClazz);
        if (implClass == null) {
            throw new RuntimeException(
                    "Cannot find implementation for " + interfaceClazz.getName());
        }
        return implClass;
    }
}
