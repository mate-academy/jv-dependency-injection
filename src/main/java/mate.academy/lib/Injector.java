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
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> implementationMap = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClass) {
        if (interfaceClass == null) {
            throw new RuntimeException("InterfaceClass is null.");
        }
        Class<?> implementationClass = findImplementation(interfaceClass);
        if (!implementationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class" + implementationClass + " couldn't have a instance, "
                    + "cause annotation @Component is not present.");
        }
        Object implementationInstance = null;
        Field[] declaredFields = implementationClass.getDeclaredFields();
        {
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    implementationInstance = createNewInstance(implementationClass);
                    field.setAccessible(true);
                    try {
                        field.set(implementationInstance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Can't initialize field value Class "
                                + implementationClass.getName() + ". Field: "
                                + field.getName(), e);
                    }
                }
            }
        }
        if (implementationInstance == null) {
            implementationInstance = createNewInstance(implementationClass);
        }
        return implementationInstance;
    }

    private Object createNewInstance(Class<?> implementationClass) {
        if (instances.containsKey(implementationClass)) {
            return instances.get(implementationClass);
        }
        Constructor<?> constructor = null;
        try {
            constructor = implementationClass.getConstructor();
            Object object = constructor.newInstance();
            instances.put(implementationClass, object);
            return object;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of"
                    + implementationClass.getName(),e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClass) {
        implementationMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementationMap.put(ProductService.class, ProductServiceImpl.class);
        implementationMap.put(ProductParser.class, ProductParserImpl.class);
        if (interfaceClass.isInterface()) {
            return implementationMap.get(interfaceClass);
        }
        if (implementationMap.get(interfaceClass) == null) {
            throw new RuntimeException("No implementation found for " + interfaceClass.getName());
        }
        return interfaceClass;
    }
}
