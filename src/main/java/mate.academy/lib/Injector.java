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
    private static final Map<Class<?>, Class<?>> findClassImplementation = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);

    private static final Injector injector = new Injector();

    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (!findClassImplementation.get(interfaceClazz).isAnnotationPresent(Component.class)) {
            throw new RuntimeException("error: class does not have "
                    +
                    "`@Component`, so the work stops.");
        }
        Class<?> fieldClazz = findClassImplementation.get(interfaceClazz);
        Object clazzImplementationInstance = createNewInstance(fieldClazz);
        Field[] declaredFields = fieldClazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, getInstance(field.getType()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t initialize field value. "
                            +
                            "Class: " + fieldClazz.getName()
                            +
                            "Field: " + field.getName(), e);
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can`t create a new instance of " + clazz.getName(), e);
        }
    }
}
