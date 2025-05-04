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

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object implementationInstance = null;
        Class<?> implementationClass = getImplClass(interfaceClazz);
        Field[] declaredField = implementationClass.getDeclaredFields();
        for (Field field : declaredField) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                implementationInstance = createInstanceOfClass(implementationClass);
                try {
                    field.setAccessible(true);
                    field.set(implementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Injection failed, "
                            + "missing @Component annotation in the class "
                            + interfaceClazz.getName(), e);
                }
            }
        }
        return implementationInstance == null
                ? createInstanceOfClass(implementationClass)
                : implementationInstance;
    }

    private Object createInstanceOfClass(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            if (!clazz.isAnnotationPresent(Component.class)) {
                throw new RuntimeException("Injection failed, "
                        + "missing @Component annotation in the class "
                        + clazz.getName());
            }
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of class " + clazz.getName(), e);
        }
    }

    private Class<?> getImplClass(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceAndImplementation = new HashMap<>();
        interfaceAndImplementation.put(ProductService.class, ProductServiceImpl.class);
        interfaceAndImplementation.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceAndImplementation.put(ProductParser.class, ProductParserImpl.class);
        return interfaceClazz.isInterface()
                ? interfaceAndImplementation.get(interfaceClazz)
                : interfaceClazz;
    }
}
