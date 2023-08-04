package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
    private static final Map<Class<?>, Object> instances = new HashMap<>();
    private static final Map<Class<?>, Class<?>> implementations = new HashMap<>(Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class));

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> type) {
        Field[] fields = type.getDeclaredFields();
        Class<?> classType = findImplementation(type);
        Object implementationInstance = null;
        checkIfComponent(classType);
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                implementationInstance = createNewInstance(classType);
                field.setAccessible(true);
                try {
                    field.set(implementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Injection failed, "
                            + "can't initialize field value:" + field.getName()
                            + " in " + type.getName());
                }
            }
        }
        if (implementationInstance == null) {
            implementationInstance = createNewInstance(classType);
        }
        return implementationInstance;
    }

    private void checkIfComponent(Class<?> classType) {
        if (!classType.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed, "
                    + "missing @Component annotation on the class " + classType.getName());
        }
    }

    private Object createNewInstance(Class<?> classType) {
        if (instances.containsKey(classType)) {
            return instances.get(classType);
        }
        try {
            Constructor<?> constructor = classType.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(classType, instance);
            return instance;
        } catch (NoSuchMethodException | InstantiationException
                 | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Injection failed, "
                    + "cannot create an instance of " + classType.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> classInterface) {
        if (!classInterface.isInterface()) {
            return classInterface;
        }
        return implementations.get(classInterface);
    }
}
