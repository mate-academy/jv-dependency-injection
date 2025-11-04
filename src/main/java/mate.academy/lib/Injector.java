package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instanceCache = new HashMap<>();
    private final Map<Class<?>, Class<?>> interfaceToImpl = new HashMap<>();

    private Injector() {
        interfaceToImpl.put(mate.academy.service.ProductParser.class,
                mate.academy.service.impl.ProductParserImpl.class);
        interfaceToImpl.put(mate.academy.service.FileReaderService.class,
                mate.academy.service.impl.FileReaderServiceImpl.class);
        interfaceToImpl.put(mate.academy.service.ProductService.class,
                mate.academy.service.impl.ProductServiceImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        try {
            if (interfaceClazz.isInterface()) {
                interfaceClazz = interfaceToImpl.get(interfaceClazz);
                if (interfaceClazz == null) {
                    throw new RuntimeException("No implementation found for interface");
                }
            }
            if (instanceCache.containsKey(interfaceClazz)) {
                return instanceCache.get(interfaceClazz);
            }
            if (!interfaceClazz.isAnnotationPresent(Component.class)) {
                throw new RuntimeException("Class " + interfaceClazz.getName()
                        + " is not annotated with @Component");
            }

            Object instance = interfaceClazz.getDeclaredConstructor().newInstance();
            instanceCache.put(interfaceClazz, instance);
            for (Field field: interfaceClazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(instance, fieldInstance);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of "
                    + interfaceClazz.getName(), e);
        }
    }
}
