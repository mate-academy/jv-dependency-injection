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
    private static final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Object classImplementationInstance = createNewInstance(clazz);
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can not initialize field value. Class : "
                            + clazz.getName() + ". Field : " + field.getName());
                }
            }
        }
        return classImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Component.class)) {
            if (instances.containsKey(clazz)) {
                return instances.get(clazz);
            }
            try {
                Object instance = clazz.getConstructor().newInstance();
                instances.put(clazz, instance);
                return instance;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Can't create instance of " + clazz.getName());
            }
        }
        throw new RuntimeException("Class have not annotation : Component " + clazz.getName());
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
