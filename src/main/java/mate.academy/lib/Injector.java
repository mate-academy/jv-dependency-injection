package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> interfaceToImplementation = new HashMap<>();

    private Injector() {
        interfaceToImplementation.put(mate.academy.service.FileReaderService.class,
                mate.academy.service.impl.FileReaderServiceImpl.class);
        interfaceToImplementation.put(mate.academy.service.ProductParser.class,
                mate.academy.service.impl.ProductParserImpl.class);
        interfaceToImplementation.put(mate.academy.service.ProductService.class,
                mate.academy.service.impl.ProductServiceImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }

        Class<?> implClass = interfaceToImplementation.get(interfaceClazz);
        if (implClass == null) {
            throw new RuntimeException("No implementation found for "
                    + interfaceClazz.getName());
        }

        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + implClass.getName()
                    + " is not marked with @Component");
        }

        try {
            Object implInstance = implClass.getDeclaredConstructor().newInstance();
            instances.put(interfaceClazz, implInstance);

            for (Field field : implClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object dependency = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(implInstance, dependency);
                }
            }
            return implInstance;
        } catch (Exception e) {
            throw new RuntimeException("Can't create an instance of class: "
                    + implClass.getName(), e);
        }
    }
}
