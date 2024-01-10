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
    private final Map<Class<?>, Class<?>> componentsMap;

    {
        componentsMap = new HashMap<>();
        fillComponetsMap();
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementation = getImplementation(interfaceClazz);
        if (!implementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Must be @Component " + interfaceClazz.getName());
        }
        Object newInstance = createNewInstance(implementation);
        for (Field declaredField : implementation.getDeclaredFields()) {
            if (declaredField.isAnnotationPresent(Inject.class)) {
                Object instance = getInstance(declaredField.getType());
                try {
                    declaredField.setAccessible(true);
                    declaredField.set(newInstance, instance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot initalize field " + declaredField.getName());
                }
            }
        }
        return newInstance;
    }

    private Object createNewInstance(Class<?> implementation) {
        try {
            Constructor<?> constructor = implementation.getConstructor();
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Reflection operation failed with " + implementation.getName());
        }
    }

    private Class<?> getImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return componentsMap.get(interfaceClazz);
        }
        return interfaceClazz;
    }

    private void fillComponetsMap() {
        componentsMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        componentsMap.put(ProductParser.class, ProductParserImpl.class);
        componentsMap.put(ProductService.class, ProductServiceImpl.class);
    }
}
