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
    private final Map<Class<?>, Class<?>> implementations;
    private final Map<Class<?>, Object> instances;

    public Injector() {
        this.implementations = Map.of(FileReaderService.class, FileReaderServiceImpl.class,
                ProductService.class, ProductServiceImpl.class,
                ProductParser.class, ProductParserImpl.class);
        this.instances = new HashMap<>();
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = getImplementation(interfaceClazz);
        Field[] fields;

        if (clazz.isAnnotationPresent(Component.class)) {
            Object implementationInstance = null;
            fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    implementationInstance = createNewInstance(clazz);
                    try {
                        field.setAccessible(true);
                        field.set(implementationInstance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Can't initialized Class Object: "
                                + clazz.getName()
                                + " Field: "
                                + field.getName());
                    }
                }
            }

            return implementationInstance == null
                    ? createNewInstance(clazz)
                    : implementationInstance;
        }
        throw new RuntimeException("Injection failed, missing @Component annotation on the class "
                + clazz.getName());
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        Constructor<?> constructor;
        Object newObject;
        try {
            constructor = clazz.getConstructor();
            newObject = constructor.newInstance();
            instances.put(clazz, newObject);
            return newObject;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create new Instance: " + e);
        }
    }

    private Class<?> getImplementation(Class<?> interfaceClazz) {
        return interfaceClazz.isInterface()
                ? implementations.get(interfaceClazz)
                : interfaceClazz;
    }
}
