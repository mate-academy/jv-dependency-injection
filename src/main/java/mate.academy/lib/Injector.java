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
    private static final Map<Class<?>, Class<?>> interfaceImplMap = new HashMap<>();

    static {
        interfaceImplMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplMap.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplMap.put(ProductService.class, ProductServiceImpl.class);
    }

    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementation = getImplementation(interfaceClazz);
        Object classImplementationInctance = null;
        Field[] fields = implementation.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplementationInctance = createNewInstance(implementation);
                try {
                    field.setAccessible(true);
                    field.set(classImplementationInctance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value, "
                            + "Class: " + implementation.getName()
                            + " Field: " + field.getName()
                            + " missing Inject annotation");
                }
            }
        }
        if (classImplementationInctance == null) {
            classImplementationInctance = createNewInstance(implementation);
        }
        return classImplementationInctance;
    }

    private Object createNewInstance(Class<?> implementation) {
        if (!implementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed, missing annotation @Component on class "
                    + implementation.getName() + " Can't create an instance");
        }
        if (instances.containsKey(implementation)) {
            return instances.get(implementation);
        }
        try {
            Constructor<?> constructor = implementation.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(implementation, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create new instance of Class: "
                    + implementation.getName());
        }
    }

    private Class<?> getImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return interfaceImplMap.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
