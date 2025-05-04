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
    private static final Map<Class<?>, Class<?>> IMPLEMENTATIONS_MAP = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementedClazz = findImplementation(interfaceClazz);
        isComponentAnnotationPresent(implementedClazz);
        Object implementedClazzInstance = null;

        Field[] declaredFields = implementedClazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                //creating an instance of field type
                Object instanceOfCurrentFieldType = getInstance(field.getType());

                //creating an object which consists the fields
                implementedClazzInstance = createInstance(implementedClazz);

                //setting instances to the object fields
                field.setAccessible(true);
                try {
                    field.set(implementedClazzInstance, instanceOfCurrentFieldType);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize the field value. Class: "
                                                + implementedClazz.getName() + "\n"
                                                + "Field: " + field.getName());
                }
            }
        }
        if (implementedClazzInstance == null) {
            implementedClazzInstance = createInstance(implementedClazz);
        }
        return implementedClazzInstance;
    }

    private Object createInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create an Instance of "
                                        + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return IMPLEMENTATIONS_MAP.get(interfaceClazz);
        }
        return interfaceClazz;
    }

    private void isComponentAnnotationPresent(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection was failed, because of "
                    + "missing @Component annotation for class: " + clazz.getName());
        }
    }
}
