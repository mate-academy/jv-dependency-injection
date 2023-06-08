package mate.academy.lib;

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
    private static final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            ProductService.class, ProductServiceImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class);

    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementationClass(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                // creating instance of field type
                Object fieldInstance = getInstance(field.getType());

                //creating instance of interface/class type
                clazzImplementationInstance = createNewInstance(clazz);

                //setting field instance to interface/class instance
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't inject field " + field.getName()
                            + " of class " + clazz.getName() + "\n", e);
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Object newInstance = clazz.getConstructor().newInstance();
            instances.put(clazz, newInstance);
            return newInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create new instance of class"
                    + clazz.getName() + "\n", e);
        }
    }

    private Class<?> findImplementationClass(Class<?> interfaceClazz) {
        Class<?> clazzImpl = interfaceClazz;
        if (interfaceClazz.isInterface()) {
            clazzImpl = interfaceImplementations.get(interfaceClazz);
            if (clazzImpl.isAnnotationPresent(Component.class)) {
                return clazzImpl;
            }
        }
        if (interfaceClazz.isAnnotationPresent(Component.class)) {
            return interfaceClazz;
        }
        throw new RuntimeException("Can't create instance of Class: " + clazzImpl.getName()
                + " - is not annotated with @Component");
    }
}
