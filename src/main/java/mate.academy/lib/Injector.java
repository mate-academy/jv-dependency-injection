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
    private final Map<Class<?>, Object> instanceOfClasses = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object newInstanceOfClass = null;
        Class<?> clazz = findClassExtandingInterface(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed,"
                    + " missing @Component annotation on the class "
                    + interfaceClazz.getName());
        }
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object instance = getInstance(field.getType());
                newInstanceOfClass = createNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(newInstanceOfClass, instance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Value can not be initialized in this class: "
                            + clazz.getName() + "; field: " + field.getName(), e);
                }
            }
        }
        if (newInstanceOfClass == null) {
            newInstanceOfClass = createNewInstance(clazz);
        }
        return newInstanceOfClass;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instanceOfClasses.containsKey(clazz)) {
            return instanceOfClasses.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instanceOfClasses.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can not create a new instance of this class: "
                    + clazz.getName(), e);
        }
    }

    private Class<?> findClassExtandingInterface(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> implementations = new HashMap<>();
        implementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementations.put(ProductParser.class, ProductParserImpl.class);
        implementations.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return implementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
