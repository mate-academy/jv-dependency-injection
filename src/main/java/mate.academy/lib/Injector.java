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

    static {
        interfaceImplementations =
                Map.of(FileReaderService.class, FileReaderServiceImpl.class,
                       ProductParser.class, ProductParserImpl.class,
                       ProductService.class, ProductServiceImpl.class
                );
    }

    private static final Injector injector = new Injector();
    private static final Map<Class<?>, Class<?>> interfaceImplementations;
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {

        Object clazzImplementationInstance = null;
        Class<?> clazzImplementation = findImplementation(interfaceClazz);
        if (!clazzImplementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class implementation: "
                                               + clazzImplementation.getName()
                                               + "doesn't have 'Component' annotation ");
        }
        Field[] fields = clazzImplementation.getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldTypeObject = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazzImplementation);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldTypeObject);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field: " + field.getName());
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazzImplementation);
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazzImplementation) {
        if (instances.containsKey(clazzImplementation)) {
            return instances.get(clazzImplementation);
        }
        try {
            Constructor<?> constructor = clazzImplementation.getConstructor();
            Object newInstance = constructor.newInstance();
            instances.put(clazzImplementation, newInstance);
            return newInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of class: "
                                               + clazzImplementation.getName());
        }
    }

    private Class<?> findImplementation(Class<?> clazz) {
        if (clazz.isInterface()) {
            return interfaceImplementations.get(clazz);
        }
        throw new RuntimeException("Unsupported type class: " + clazz.getName());
    }
}
