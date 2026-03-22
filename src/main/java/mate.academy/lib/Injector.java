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
    private static final Map<Class<?>, Class<?>> implementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );
    private static final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementationClazz = findImplementation(interfaceClazz);
        return createNewInstance(implementationClazz);
    }

    private Object createNewInstance(Class<?> implementationClazz) {
        if (instances.containsKey(implementationClazz)) {
            return instances.get(implementationClazz);
        }

        try {
            Constructor<?> constructor = implementationClazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(implementationClazz, instance);

            // инжектим поля только при создании нового инстанса
            Field[] fields = implementationClazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    try {
                        field.setAccessible(true);
                        field.set(instance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Can't initialize field. "
                                + "Type: " + implementationClazz.getName()
                                + ". Field: " + field.getName(), e); // добавили e
                    }
                }
            }

            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't get instance of "
                    + implementationClazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface() && interfaceClazz.isAnnotationPresent(Component.class)) {
            return interfaceClazz;
        }

        Class<?> implementation = implementations.get(interfaceClazz);

        if (implementation == null) {
            throw new RuntimeException("Can't initialize for unsupported class. "
                    + "Class "
                    + interfaceClazz.getName()
                    + "does not have an implementations"
                    + "in this project");
        }

        if (!implementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Interface " + interfaceClazz.getSimpleName()
                    + " implementations are not annotated with @Component annotation");
        }

        return implementation;
    }
}
