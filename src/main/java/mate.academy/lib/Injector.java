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
    private static final Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
    private static final Map<Class<?>, Object> instancedImplementations = new HashMap<>();

    public static Injector getInjector() {
        if (interfaceImplementations.isEmpty()) {
            interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
            interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
            interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        }
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = getImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(interfaceClazz.getName()
                    + "Is not supported. (Must annotated with Component)");
        }
        Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .forEach(field -> {
                    field.setAccessible(true);
                    try {
                        field.set(
                                createNewInstance(clazz),
                                getInstance(field.getType())
                        );
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Error while injecting instance into field "
                                + field.getName() + ", of class " + clazz.getName(), e);
                    }
                });
        return createNewInstance(clazz);
    }

    private static Object createNewInstance(Class<?> clazz) {
        Object foundInstance = instancedImplementations.get(clazz);
        if (foundInstance != null) {
            return foundInstance;
        }
        try {
            Object instance = clazz.getConstructor().newInstance();
            instancedImplementations.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Got error while instancing class with name "
                    + clazz.getName(), e);
        }
    }

    private static Class<?> getImplementation(Class<?> interfaceClazz) {
        return interfaceClazz.isInterface()
               ? interfaceImplementations.get(interfaceClazz)
               : interfaceImplementations
                       .keySet()
                       .stream()
                       .filter(interfaceClazz::equals)
                       .findFirst()
                       .orElseThrow();
    }
}
