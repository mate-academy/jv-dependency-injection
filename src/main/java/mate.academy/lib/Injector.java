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

    private static final Map<Class<?>, Class<?>> implementationClasses = new HashMap<>();

    static {
        implementationClasses.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementationClasses.put(ProductParser.class, ProductParserImpl.class);
        implementationClasses.put(ProductService.class, ProductServiceImpl.class);
    }

    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object classImplementationInstance = null;
        Class<?> implementationClass = findImplementationClass(interfaceClazz);

        if (!implementationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed, missing @Component"
                   + " annotaion on the class " + implementationClass);
        }
        Field[] declaredFields = implementationClass.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());

                classImplementationInstance = createNewInstance(implementationClass);

                try {
                    field.setAccessible(true);
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialise field value", e);
                }
            }
        }
        if (classImplementationInstance == null) {
            classImplementationInstance = createNewInstance(implementationClass);
        }
        return classImplementationInstance;
    }

    private Object createNewInstance(Class<?> implementationClass) {
        if (instances.containsKey(implementationClass)) {
            return instances.get(implementationClass);
        }
        try {
            Constructor<?> constructor = implementationClass.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(implementationClass, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of "
                    + implementationClass.getName(), e);
        }
    }

    private static Class<?> findImplementationClass(Class<?> interfaceClass) {
        if (interfaceClass.isInterface()) {
            return implementationClasses.get(interfaceClass);
        }
        return interfaceClass;
    }
}
