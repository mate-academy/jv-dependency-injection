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

    private final Map<Class<?>, Object> instanceMap = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {

        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Annotation @Component not found in "
                    + interfaceClazz.getName());
        }
        Object clazzInstance = createNewInstance(clazz);
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().isPrimitive()) {
                continue;
            }
            Object fieldInstance = getInstance(field.getType());
            clazzInstance = createNewInstance(clazz);
            try {
                field.setAccessible(true);
                field.set(clazzInstance, fieldInstance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("can't set field" + field.getName()
                        + " in class " + clazz.getName(), e);
            }
        }
        if (clazzInstance == null) {
            clazzInstance = createNewInstance(interfaceClazz);
        }
        return clazzInstance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplMap = new HashMap<>();
        interfaceImplMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplMap.put(ProductService.class, ProductServiceImpl.class);
        interfaceImplMap.put(ProductParser.class, ProductParserImpl.class);
        if (interfaceClazz.isInterface()) {
            return interfaceImplMap.get(interfaceClazz);
        }
        return interfaceClazz;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instanceMap.containsKey(clazz)) {
            return instanceMap.get(clazz);
        }
        Constructor<?> constructor = null;
        Object instance = null;
        try {
            constructor = clazz.getConstructor();
            instance = constructor.newInstance();
        } catch (NoSuchMethodException
                | InstantiationException
                | IllegalAccessException
                | InvocationTargetException e) {
            throw new RuntimeException("can't create instance", e);
        }
        instanceMap.put(clazz, instance);
        return instance;
    }
}
