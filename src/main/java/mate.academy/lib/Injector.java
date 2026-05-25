package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();

    private final Map<Class<?>, Object> instances = new HashMap<>();

    private final Map<String, String> implementationMap = Map.of(
            "FileReaderService", "mate.academy.service.impl.FileReaderServiceImpl",
            "ProductParser", "mate.academy.service.impl.ProductParserImpl",
            "ProductService", "mate.academy.service.impl.ProductServiceImpl"
    );

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) throws ClassNotFoundException {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed: Class "
                    + clazz.getName() + " is not marked with @Component");
        }
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            Object classInstance = constructor.newInstance();
            instances.put(clazz, classInstance);
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(classInstance, fieldInstance);
                }
            }
            return classInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of class: " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) throws ClassNotFoundException {
        if (!interfaceClazz.isInterface()) {
            return interfaceClazz;
        }
        String interfaceName = interfaceClazz.getSimpleName();
        String className = implementationMap.get(interfaceName);
        if (className == null) {
            throw new RuntimeException("Wrong implementation");
        }
        return Class.forName(className);
    }
}
