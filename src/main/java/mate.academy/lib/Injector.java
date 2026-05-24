package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();

    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
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
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of class: " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            return interfaceClazz;
        }
        String interfaceName = interfaceClazz.getSimpleName();
        try {
            switch (interfaceName) {
                case "FileReaderService":
                    return Class.forName("mate.academy.service.impl.FileReaderServiceImpl");
                case "ProductParser":
                    return Class.forName("mate.academy.service.impl.ProductParserImpl");
                case "ProductService":
                    return Class.forName("mate.academy.service.impl.ProductServiceImpl");
                default:
                    throw new RuntimeException("No implementation found for interface: "
                            + interfaceClazz.getName());
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not find implementation class for "
                    + interfaceName, e);
        }
    }
}
