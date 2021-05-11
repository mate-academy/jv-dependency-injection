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
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClass) {
        Object interfaceClassInstance = null;
        Class<?> interfaceImplClass = findImplementation(interfaceClass);
        Field[] fields = interfaceImplClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                interfaceClassInstance = createNewInstance(interfaceImplClass);
                try {
                    field.setAccessible(true);
                    field.set(interfaceClassInstance, fieldInstance);
                } catch (IllegalAccessException exception) {
                    throw new RuntimeException("Can't initialize field "
                            + field.getName() + " in Class "
                            + interfaceImplClass.getName(), exception);
                }
            }
        }
        return interfaceClassInstance == null ? createNewInstance(interfaceImplClass)
                : interfaceClassInstance;
    }

    private Object createNewInstance(Class<?> interfaceClassImpl) {
        if (instances.containsKey(interfaceClassImpl)) {
            return instances.get(interfaceClassImpl);
        }
        try {
            Object instance = interfaceClassImpl.getConstructor().newInstance();
            instances.put(interfaceClassImpl, instance);
            return instance;
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("Can't create new instance of: "
                    + interfaceClassImpl.getName(), exception);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClass) {
        Map<Class<?>, Class<?>> interfaceImplementation = new HashMap<>();
        interfaceImplementation.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementation.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplementation.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClass.isInterface()) {
            Class<?> interfaceClassImpl = interfaceImplementation.get(interfaceClass);
            if (!interfaceClassImpl.isAnnotationPresent(Component.class)) {
                throw new RuntimeException(interfaceClassImpl.getName()
                        + " is not marked with annotation for instantiation");
            }
            return interfaceClassImpl;
        }
        return interfaceClass;
    }
}
