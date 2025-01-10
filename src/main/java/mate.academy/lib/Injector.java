package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> clazz) {
        Class<?> implementation = findImplementation(clazz);
        Object instance = null;
        Field[] fields = implementation.getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object newInstance = getInstance(field.getType());
                instance = createNewInstance(implementation);
                field.setAccessible(true);
                try {
                    field.set(instance, newInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (instance == null) {
            instance = createNewInstance(implementation);
        }
        return instance;
    }

    private Object createNewInstance(Class<?> implementation) {
        if (instances.containsKey(implementation)) {
            return instances.get(implementation);
        }

        try {
            Constructor<?>[] constructors = implementation.getConstructors();
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterCount() > 0) {
                    Class<?>[] parameterTypes = constructor.getParameterTypes();
                    Object[] objects = new Object[parameterTypes.length];
                    for (int i = 0; i < objects.length; i++) {
                        objects[i] = getInstance(parameterTypes[i]);
                    }
                    return constructor.newInstance(objects);
                }
            }
        } catch (InvocationTargetException
                 | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        try {
            Constructor<?> constructor = implementation.getConstructor();
            Object object = constructor.newInstance();
            instances.put(implementation, object);
            return constructor.newInstance();
        } catch (NoSuchMethodException | IllegalAccessException
                 | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> findImplementation(Class<?> clazz) {
        Map<Class<?>,Class<?>> implemetations = new HashMap<>();
        implemetations.put(FileReaderService.class, FileReaderServiceImpl.class);
        implemetations.put(ProductParser.class, ProductParserImpl.class);
        implemetations.put(ProductService.class, ProductServiceImpl.class);
        if (implemetations.containsKey(clazz)) {
            return implemetations.get(clazz);
        }
        return clazz;
    }
}

/* private static final Injector injector = new Injector();
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

        public Object getInstance(Class<?> interfaceClazz) {
            Object clazzImplement = null;
            Class<?> clazz = findImplementation(interfaceClazz);
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    clazzImplement = createNewInstance(clazz);
                    try {
                        field.setAccessible(true);
                        field.set(clazzImplement, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            if (clazzImplement == null) {
                clazzImplement = createNewInstance(clazz);
            }
            return clazzImplement;
        }

        private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
            try {
                Constructor<?>[] constructors = clazz.getConstructors();
                for (Constructor<?> constructor : constructors) {
                    // Перевіряємо, чи є конструктор з параметрами
                    if (constructor.getParameterCount() > 0) {
                        Class<?>[] parameterTypes = constructor.getParameterTypes();
                        Object[] parameters = new Object[parameterTypes.length];
                        for (int i = 0; i < parameterTypes.length; i++) {
                            parameters[i] = getInstance(parameterTypes[i]); // Інжектуємо залежності
                        }
                        return constructor.newInstance(parameters);
                    }
                }
                Constructor<?> constructor = clazz.getConstructor();
                Object instance = constructor.newInstance();
                instances.put(clazz, instance);
                return instance;
            } catch (NoSuchMethodException | InvocationTargetException
                     | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        private Class<?> findImplementation(Class<?> interfaceClazz) {
            Map<Class<?> , Class<?>> interfaceImplementations = new HashMap<>();
            interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
            interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
            interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
            if (interfaceClazz.isInterface()) {
                return interfaceImplementations.get(interfaceClazz);
            }
            return interfaceClazz;
        }
     */
