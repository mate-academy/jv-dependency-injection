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

    private final Map<Class<?>, Class<?>> interfaceImpl =
            Map.of(FileReaderService.class, FileReaderServiceImpl.class, ProductParser.class,
                    ProductParserImpl.class, ProductService.class, ProductServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {

        Class<?> implClass = getImplclass(interfaceClazz);

        Object instance = createNewInstance(implClass);

        Field[] declaredFields = implClass.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object dependency = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(instance, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                            "Can't initialize field value. " + "Class: " + implClass.getName()
                                    + " Field: " + field.getName(), e);
                }
            }
        }
        return instance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Injection failed: missing @Component on class " + clazz.getName());
        }
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of" + clazz.getName(), e);
        }
    }

    private Class<?> getImplclass(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            if (interfaceImpl.get(interfaceClazz) == null) {
                throw new RuntimeException("There is no implementation for" + interfaceClazz);
            }
            return interfaceImpl.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
