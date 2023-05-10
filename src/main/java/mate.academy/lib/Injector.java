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
    private Map<Class<?>, Object> instancesMap = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationObject = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field: declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationObject = createNewObject(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationObject, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t initialize field value");
                }
            }
        }
        if (clazzImplementationObject == null) {
            clazzImplementationObject = createNewObject(clazz);
        }
        return clazzImplementationObject;
    }

    private Object createNewObject(Class<?> clazz) {
        if (instancesMap.containsKey(clazz)) {
            return instancesMap.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object newInstance = constructor.newInstance();
            instancesMap.put(clazz, newInstance);
            return newInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can`t create new instance of: " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> classMap = new HashMap<>();
        classMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        classMap.put(ProductParser.class, ProductParserImpl.class);
        classMap.put(ProductService.class, ProductServiceImpl.class);
        if (!interfaceClazz.isInterface()
                && interfaceClazz.isAnnotationPresent(Component.class)) {
            return interfaceClazz;
        }
        if (interfaceClazz.isInterface()
                && classMap.get(interfaceClazz).isAnnotationPresent(Component.class)) {
            return classMap.get(interfaceClazz);
        }
        throw new RuntimeException("Injection failed, missing @Component annotation on the class "
                + interfaceClazz.getName());
    }
}
