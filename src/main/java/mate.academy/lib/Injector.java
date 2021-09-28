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
    private final Map<Class<?>, Object> classInstances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        clazzImplementationInstance = createNewInstance(clazz);
        for (Field field : declaredFields) {
            if (!clazz.isAnnotationPresent(Component.class)) {
                throw new RuntimeException("Annotation @Component doesn't exist in class: "
                        + clazz.getName());
            }
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. "
                            + "Class: " + clazz.getName()
                            + " Field: " + field.getName());
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (classInstances.containsKey(clazz)) {
            return classInstances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            classInstances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create new instance " + clazz.getSimpleName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceOrImplementation) {
        Map<Class<?>, Class<?>> interfaceAndImplementation = new HashMap<>();
        interfaceAndImplementation.put(ProductService.class, ProductServiceImpl.class);
        interfaceAndImplementation.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceAndImplementation.put(ProductParser.class, ProductParserImpl.class);
        if (interfaceOrImplementation.isInterface()) {
            return interfaceAndImplementation.get(interfaceOrImplementation);
        }
        return interfaceOrImplementation;
    }
}
