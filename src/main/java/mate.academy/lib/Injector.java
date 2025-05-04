package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>,Object> instanceCache = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (instanceCache.containsKey(interfaceClazz)) {
            return instanceCache.get(interfaceClazz);
        }
        try {
            Class<?> implementationClass = findImplementation(interfaceClazz);
            if (!implementationClass.isAnnotationPresent(Component.class)) {
                throw new RuntimeException("Class "
                        + implementationClass.getName()
                        + " is not marked with @Component");
            }
            Constructor<?> constructor = implementationClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object instance = constructor.newInstance();
            instanceCache.put(interfaceClazz,instance);
            initializeDependencies(instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of "
            + interfaceClazz.getName(),e);
        }
    }

    public void initializeDependencies(Object instance) throws IllegalAccessException {
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field:fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                Object denendency = getInstance(field.getType());
                field.set(instance,denendency);
            }
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            return interfaceClazz;
        }
        if (interfaceClazz == mate.academy.service.FileReaderService.class) {
            return mate.academy.service.impl.FileReaderServiceImpl.class;
        } else if (interfaceClazz == mate.academy.service.ProductParser.class) {
            return mate.academy.service.impl.ProductParserImpl.class;
        } else if (interfaceClazz == mate.academy.service.ProductService.class) {
            return mate.academy.service.impl.ProductServiceImpl.class;
        }
        throw new RuntimeException("No implementation found for interface: "
                + interfaceClazz.getName());
    }
}
