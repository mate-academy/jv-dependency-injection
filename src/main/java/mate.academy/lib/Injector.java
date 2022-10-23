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

@Component
public class Injector {
    private static final Injector injector = new Injector();
    private static Map<Class<?>,Object> instances = new HashMap<>();
    private static Map<Class<?>, Class<?>> interfaceImplementations;

    public Injector() {
        interfaceImplementations = new HashMap<>();
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (!interfaceClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Cant create instance");
        }
        Object clazzImplamentationInstance = null;
        Class<?> clazz = findImplamentation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fildInstance = getInstance(field.getType());
                clazzImplamentationInstance = createNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplamentationInstance,fildInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                            "Cant initialise field value. Class: " + clazz.getName()
                                    + "Field" + field.getName());
                }
            }
        }
        if (clazzImplamentationInstance == null) {
            clazzImplamentationInstance = createNewInstance(clazz);
        }
        return clazzImplamentationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        Constructor<?> constructor = null;
        try {
            constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz,instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Cant create an instance of " + clazz.getName());
        }
    }

    private Class<?> findImplamentation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
