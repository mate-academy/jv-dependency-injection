package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> interfaceImplementation = Map.of(
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Component annotation = clazz.getAnnotation(Component.class);
        if (annotation == null) {
            throw new RuntimeException("@Component annotation missed on the class "
                    + clazz.getName());
        }

        return Stream.of(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .map(field -> {
                    Object fieldInstance = getInstance(field.getType());
                    Object instance = createNewInstance(clazz);
                    try {
                        field.setAccessible(true);
                        field.set(instance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Initialization of field value is failed. "
                                + "Class: " + clazz.getName() + ". Field: " + field.getName(), e);
                    }
                    return instance;
                })
                .findFirst()
                .orElseGet(() -> createNewInstance(clazz));
    }

    private Object createNewInstance(Class<?> clazz) {
        return instances.computeIfAbsent(clazz, key -> {
            try {
                Constructor<?> constructor = clazz.getConstructor();
                return constructor.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("A new instance of can't be created in "
                        + clazz.getName(), e);
            }
        });
    }

    private Class<?> findImplementation(Class<?> interfaceOrClazz) {
        return interfaceOrClazz.isInterface() ? interfaceImplementation.get(interfaceOrClazz)
                : interfaceOrClazz;
    }
}
