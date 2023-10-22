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
    private Map<Class<?>, Class<?>> implementaionMap = new HashMap<>();
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementatiomInstance = null;
        Class<?> implementationClazz = findImplementation(interfaceClazz);
        if (!implementationClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed,"
                    + " missing @Component annotation on the class "
                    + implementationClazz.getName());
        }
        Field [] declaredField = implementationClazz.getDeclaredFields();
        for (Field field : declaredField) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementatiomInstance = createNewInstance(implementationClazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementatiomInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class:"
                            + implementationClazz.getName());
                }
            }
        }
        if (clazzImplementatiomInstance == null) {
            clazzImplementatiomInstance = createNewInstance(implementationClazz);
        }
        return clazzImplementatiomInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return implementaionMap.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object clazzInstance = constructor.newInstance();
            instances.put(clazz.getClass(), clazzInstance);
            return clazzInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getClass());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        implementaionMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementaionMap.put(ProductParser.class, ProductParserImpl.class);
        implementaionMap.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return implementaionMap.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
