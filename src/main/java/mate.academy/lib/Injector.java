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
    private static Map<Class<?>, Object> instances;

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {

        Class<?> implClazz = getImpl(interfaceClazz);
        Object clazzImplementationObject;
        if (!implClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Annotation @Component is not present in the class: "
                    + implClazz.getName());
        }
        clazzImplementationObject = createNewInstance(implClazz);
        Field[] declaredFields = implClazz.getDeclaredFields();
        for (Field field: declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationObject, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't set the field: \"" + field.getName()
                            + "\" of the class: \"" + implClazz.getName() + "\"");
                }

            }
        }
        return clazzImplementationObject;
    }

    private Object createNewInstance(Class<?> implClazz) {
        if (instances == null) {
            instances = new HashMap<>();
        } else if (instances.containsKey(implClazz)) {
            return instances.get(implClazz);
        }
        try {
            Constructor<?> constructor = implClazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(implClazz, instance);
            return instance;

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create new object of the class: "
                    + implClazz.getName(), e);
        }
    }

    private Class<?> getImpl(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> clazzImplementationMap = Map.of(
                FileReaderService.class, FileReaderServiceImpl.class,
                ProductParser.class, ProductParserImpl.class,
                ProductService.class, ProductServiceImpl.class);
        return clazzImplementationMap.get(interfaceClazz);
    }
}
