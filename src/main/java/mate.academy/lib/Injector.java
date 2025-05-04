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

    private static final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazzImplementation = findImplementation(interfaceClazz);

        if (!clazzImplementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed: missing @Component "
                    + "annotation on the class " + clazzImplementation.getName());
        }
        clazzImplementationInstance = createNewInstance(clazzImplementation);

        Field[] fields = clazzImplementation.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldTypeObject = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldTypeObject);
                } catch (IllegalAccessException e) {

                    throw new RuntimeException("Injection failed: can't initialize field "
                            + field.getName() + " in class " + clazzImplementation.getName(), e);
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazzImplementation) {

        if (!clazzImplementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't create instance of class: "
                    + clazzImplementation.getName()
                    + " because it is missing @Component annotation.");
        }
        if (instances.containsKey(clazzImplementation)) {
            return instances.get(clazzImplementation);
        }
        try {
            Constructor<?> constructor = clazzImplementation.getConstructor();
            Object newInstance = constructor.newInstance();
            instances.put(clazzImplementation, newInstance);
            return newInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Creation failed: can't create instance of class "
                    + clazzImplementation.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> clazz) {
        if (clazz.isInterface()) {
            Class<?> implementation = interfaceImplementations.get(clazz);
            if (implementation == null) {
                throw new RuntimeException("Implementation not found for interface: "
                        + clazz.getName());
            }
            return implementation;
        }
        throw new RuntimeException("Expected an interface but got class: " + clazz.getName());
    }
}
