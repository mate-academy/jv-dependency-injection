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
    private static final Map<Class<?>,Object> instances = new HashMap<>();
    private static final Map<Class<?>, Class<?>> interfaceImpl
            = new HashMap<Class<?>, Class<?>>() {{
                    put(FileReaderService.class, FileReaderServiceImpl.class);
                    put(ProductParser.class, ProductParserImpl.class);
                    put(ProductService.class, ProductServiceImpl.class);
                }};

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object classImplInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();

        for (Field field: declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplInstance = createNewInstance(clazz);
            }
            
        }
        if (classImplInstance == null) {
            classImplInstance = createNewInstance(clazz);
        }
        return classImplInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz,instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can`t create instance " + clazz.getName());
        }

    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return interfaceImpl.get(interfaceClazz);
        }
        throw new RuntimeException("Unsupported class " + interfaceClazz.getName());
    }
}
