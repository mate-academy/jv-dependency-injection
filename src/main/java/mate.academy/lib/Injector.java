package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();

    private static final Map<Class<?>, Class<?>> interfaceImplMap = Map.of(
            mate.academy.service.ProductService.class,
            mate.academy.service.impl.ProductServiceImpl.class,
            mate.academy.service.FileReaderService.class,
            mate.academy.service.impl.FileReaderServiceImpl.class,
            mate.academy.service.ProductParser.class,
            mate.academy.service.impl.ProductParserImpl.class
    );

    private final Map<Class<?>, Object> instanceCache = new HashMap<>();

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public <T> T getInstance(Class<T> clazz) {
        Class<?> implClazz = interfaceImplMap.get(clazz);

        if (implClazz == null) {
            if (clazz.isInterface()) {
                throw new RuntimeException(
                        "Injection failed: no implementation found for interface "
                                + clazz.getName()
                );
            } else {
                implClazz = clazz;
            }
        }

        if (!implClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Injection failed, missing @Component annotation on the class "
                            + implClazz.getName()
            );
        }

        if (instanceCache.containsKey(implClazz)) {
            return (T) instanceCache.get(implClazz);
        }

        try {
            Object implInstance = implClazz.getDeclaredConstructor().newInstance();
            instanceCache.put(implClazz, implInstance);

            for (Field field : implClazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object dependency = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(implInstance, dependency);
                }
            }

            return (T) implInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Injection failed, error while creating instance of class "
                            + implClazz.getName(),
                    e
            );
        }
    }
}
