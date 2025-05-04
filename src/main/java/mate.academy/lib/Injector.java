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
    private static final String MISSING_COMPONENT_ANNOTATION
            = "Missing @Component annotation, can't be able to create an instance of this class: ";
    private static final String CANT_INITIALIZE_FIELD
            = "Can't initialize field: %s in the class: %s";
    private static final String CANT_CREATE_INSTANCE = "Can't create an instance of: ";
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> classImplementations
            = Map.of(FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(clazz.getCanonicalName() + MISSING_COMPONENT_ANNOTATION
                    + clazz.getCanonicalName());
        }
        Object clazzImplementationInstance = createNewInstance(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(String.format(
                            CANT_INITIALIZE_FIELD, field.getName(), clazz.getName()), e);
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        return instances.computeIfAbsent(clazz, key -> {
            try {
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(CANT_CREATE_INSTANCE + clazz.getName(), e);
            }
        });
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        return interfaceClazz.isInterface()
                ? classImplementations.get(interfaceClazz)
                : interfaceClazz;
    }
}
