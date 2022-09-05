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
    private Map<Class<?>, Object> instances = new HashMap<>();
    private Map<Class<?>, Class<?>> implementationMap = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> realizationClass = findRealizationClass(interfaceClazz);
        if (!realizationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class must be marked as Component "
                    + realizationClass.getName());
        }
        Object result = null;
        Field[] fields = realizationClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                result = generateInstance(realizationClass);
                try {
                    field.setAccessible(true);
                    field.set(result, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field " + field.getName(), e);
                }
            }
        }
        return result == null ? generateInstance(realizationClass) : result;
    }

    private Class<?> findRealizationClass(Class<?> interfaceClazz) {
        implementationMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementationMap.put(ProductService.class, ProductServiceImpl.class);
        implementationMap.put(ProductParser.class, ProductParserImpl.class);
        return interfaceClazz.isInterface()
                ? implementationMap.get(interfaceClazz)
                : interfaceClazz;
    }

    private Object generateInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't generate instance of " + clazz.getName(), e);
        }
    }
}
