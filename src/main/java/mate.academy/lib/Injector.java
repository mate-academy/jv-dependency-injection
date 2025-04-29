package mate.academy.lib;

import java.util.Arrays;
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
    private static final Map<Class<?>, Class<?>> interfaceImpl;
    private final Map<Class<?>, Object> clazzInstance = new HashMap<>();

    static {
        interfaceImpl = Map.of(
          FileReaderService.class, FileReaderServiceImpl.class,
          ProductParser.class, ProductParserImpl.class,
          ProductService.class, ProductServiceImpl.class
        );
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implClazz = getImplementationClazz(interfaceClazz);
        if (!implClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't create instance of "
                                        + interfaceClazz.getName()
                                        + ": " + implClazz.getName()
                                        + " isn't annotated as Component");
        }

        Object implInstance = createImplInstance(implClazz);
        Arrays.stream(implClazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .forEach(field -> {
                    Object fieldInstance = getInstance(field.getType());
                    field.setAccessible(true);
                    try {
                        field.set(implInstance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Can't set field " + field.getName()
                                                    + " for class " + implClazz.getName());
                    }
                });
        return implInstance;
    }

    private Object createImplInstance(Class<?> clazz) {
        Object instance = clazzInstance.get(clazz);
        if (instance != null) {
            return instance;
        }

        try {
            instance = clazz.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of type " + clazz.getName(), e);
        }
        clazzInstance.put(clazz, instance);
        return instance;
    }

    private Class<?> getImplementationClazz(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            return interfaceClazz;
        }

        Class<?> implClazz = interfaceImpl.get(interfaceClazz);
        if (implClazz == null) {
            throw new RuntimeException("Can't find implementation for interface: "
                                        + interfaceClazz.getName());
        }
        return implClazz;
    }
}
