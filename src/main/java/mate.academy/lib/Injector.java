package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private static final Map<Class<?>, Class<?>> implementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);
    private static final Map<Class<?>, Object> instances = new HashMap<>();
    private static final String NO_ANNOTATION
            = "Could not find class of %s annotated with @Component";
    private static final String CREATION_EXCEPTION = "Could not create instance of class ";
    private static final String FIELD_INIT_EXCEPTION = "Can`t initialize field ";

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        final Class<?> clazz = findImplementation(interfaceClazz);
        final Object result = createNewInstance(clazz);
        final List<Field> fieldsToInject = Arrays.stream(clazz.getFields())
                .filter(f -> f.isAnnotationPresent(Inject.class))
                .collect(Collectors.toList());
        for (Field field : fieldsToInject) {
            Object fieldInstance = getInstance(field.getClass());
            field.setAccessible(true);
            try {
                field.set(result, fieldInstance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(FIELD_INIT_EXCEPTION + field.getName(), e);
            }
        }
        return result;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            final Constructor<?> constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            final Object created = constructor.newInstance();
            instances.put(clazz, created);
            return created;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(CREATION_EXCEPTION + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Class<?> impl = interfaceClazz;
        if (implementations.containsKey(interfaceClazz)) {
            impl = implementations.get(interfaceClazz);
        }
        if (!impl.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    String.format(NO_ANNOTATION, interfaceClazz.getName()));
        }
        return impl;
    }
}
