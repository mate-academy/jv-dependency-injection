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
    private static final Map<Class<?>, Class<?>> INTERFACE_IMPLEMENTATIONS = new HashMap<>() {
        {
            put(FileReaderService.class, FileReaderServiceImpl.class);
            put(ProductParser.class, ProductParserImpl.class);
            put(ProductService.class, ProductServiceImpl.class);
        }
    };
    private static final Injector injector = new Injector();
    private Map<Class<?>, Object> instanses = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (instanses.containsKey(clazz)) {
            return instanses.get(clazz);
        }
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("No @Component annotation present in class: "
                    + clazz.getName());
        }
        Field[] declaredFields = clazz.getDeclaredFields();
        Object clazzImplementationInstance = null;
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class: "
                            + clazz.getName() + ". Field " + field.getName());
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instanses.containsKey(clazz)) {
            return instanses.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instanses.put(clazz, instance);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName(), e);
        }
        return instanses.get(clazz);

    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isAnnotationPresent(Component.class)) {
            return interfaceClazz;
        }
        if (interfaceClazz.isInterface()) {
            return INTERFACE_IMPLEMENTATIONS.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
