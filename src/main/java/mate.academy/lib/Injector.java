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
    private static final Map<Class<?>, Class<?>> implementationMap =
            Map.of(FileReaderService.class, FileReaderServiceImpl.class,
                ProductParser.class, ProductParserImpl.class,
                ProductService.class, ProductServiceImpl.class);

    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        checkIfClassIsComponent(clazz);
        Object clazzImplementationInstance = createNewInstance(clazz);
        injectFields(clazzImplementationInstance);
        return clazzImplementationInstance;
    }

    private void checkIfClassIsComponent(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + clazz.getName()
                                                + " is missing @Component annotation.");
        }
    }

    private void injectFields(Object instance) {
        Field[] declaredFields = instance.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. "
                        + "Class " + instance.getClass().getName()
                        + ", field " + field.getName(), e);
                }
            }
        }
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
            throw new RuntimeException("Can't create a new instance of " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return Injector.implementationMap.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
