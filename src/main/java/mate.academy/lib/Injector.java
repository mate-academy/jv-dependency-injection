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
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzImplementationInstance = createNewInstance(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                // create an object of filed typ
                Object fieldInstance = getInstance(field.getType());
                // create ann object of interfaceClazz type or implementation class.
                // I think it should be before for loop,
                // because we don't want to create a new instance for each field
                // we want to create it once and then inject it into the fields.
                // So I will move this line before the for loop.
                //set field value to the created object
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                            "Can't set field value: "
                                    + field.getName()
                                    + " in class: "
                                    + clazz.getName(), e);
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            Object newInstance = constructor.newInstance();
            instances.put(clazz, newInstance);
            return newInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Can't create instance of class: " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> implementationMap = new HashMap<>();
        implementationMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementationMap.put(ProductParser.class, ProductParserImpl.class);
        implementationMap.put(ProductService.class, ProductServiceImpl.class);

        Class<?> implementation = interfaceClazz.isInterface()
                ? implementationMap.get(interfaceClazz)
                : interfaceClazz;
        if (implementation == null
                || !implementation.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("No implementation found for: " + interfaceClazz.getName());
        }
        return implementation;
    }
}
