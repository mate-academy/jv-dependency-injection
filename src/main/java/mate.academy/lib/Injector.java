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
    private static final Map<Class<?>, Class<?>> INTERFACE_IMPLEMENTATIONS
            = Map.of(FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);
    private Map<Class<?>, Object> instaces = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) throws RuntimeException {
        Object classImplementationInstance = null;
        Class<?> implementationClazz = findImplementation(interfaceClazz);
        Field[] fields = implementationClazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplementationInstance = getOrCreateInstance(implementationClazz);
                try {
                    field.setAccessible(true);
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t initialize field value "
                            + field.getName()
                            + "Class " + implementationClazz.getName(), e);
                }
            }
        }
        if (implementationClazz.isAnnotationPresent(Component.class)) {
            classImplementationInstance = getOrCreateInstance(implementationClazz);;
        } else {
            throw new RuntimeException("Injection failed, missing "
                    + Component.class.getName() + " annotaion on the class: "
                    + implementationClazz.getName());
        }
        return classImplementationInstance;
    }

    private Object getOrCreateInstance(Class<?> clazz) throws RuntimeException {
        if (instaces.containsKey(clazz)) {
            return instaces.get(clazz);
        }
        Constructor<?> constructor = null;
        try {
            constructor = clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        Object instance = null;
        try {
            instance = constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Object can`t be generated", e);
        }
        instaces.put(clazz, instance);
        return instance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return INTERFACE_IMPLEMENTATIONS.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
