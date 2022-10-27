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
    private static Map<Class<?>, Class<?>> implementationMap;
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        implementationMap = fillDefaultStorage();
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> implementation = findImplementation(interfaceClazz);
        if (!implementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Injection failed, missing @Component annotation on the class "
                    + interfaceClazz.getName());
        }
        Field[] fields = implementation.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());

                clazzImplementationInstance = createNewInstance(implementation);

                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t modify field "
                            + field.getName() + " in " + interfaceClazz.getName(), e);
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(implementation);
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        Object instance;
        try {
            instance = clazz.getConstructor().newInstance();
            instances.put(clazz, instance);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can`t create instance of " + clazz.getName(), e);
        }
        return instance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return implementationMap.get(interfaceClazz);
        }
        return interfaceClazz;
    }

    private static Map<Class<?>, Class<?>> fillDefaultStorage() {
        Map<Class<?>, Class<?>> interfaceImplMap = new HashMap<>();
        interfaceImplMap.put(ProductService.class, ProductServiceImpl.class);
        interfaceImplMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplMap.put(ProductParser.class, ProductParserImpl.class);
        return interfaceImplMap;
    }
}
