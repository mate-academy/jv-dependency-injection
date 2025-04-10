package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();

    private final Map<Class<?>, Class<?>> interfaceToImpl = new HashMap<>();

    private Injector() {
        interfaceToImpl.put(mate.academy.service.ProductService.class,
                mate.academy.service.impl.ProductServiceImpl.class);
        interfaceToImpl.put(mate.academy.service.FileReaderService.class,
                mate.academy.service.impl.FileReaderServiceImpl.class);
        interfaceToImpl.put(mate.academy.service.ProductParser.class,
                mate.academy.service.impl.ProductParserImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implClass = interfaceToImpl.get(interfaceClazz);
        if (implClass == null) {
            throw new RuntimeException("No implementation found for " + interfaceClazz.getName());
        }

        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't create instance of " + implClass.getName()
                    + " because it is not annotated with @Component");
        }

        try {
            Constructor<?> constructor = implClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object instance = constructor.newInstance();

            for (Field field : implClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Class<?> dependencyClass = field.getType();
                    Object dependency = getInstance(dependencyClass);
                    field.setAccessible(true);
                    field.set(instance, dependency);
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of class: " + implClass.getName(), e);
        }
    }
}
