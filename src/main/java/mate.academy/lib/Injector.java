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
    private static final Map<Class<?>, Object> instances = new HashMap<>();
    private static final Map<Class<?>, Class<?>> implementedClassesMap = Map
            .of(ProductService.class, ProductServiceImpl.class,
                    FileReaderService.class, FileReaderServiceImpl.class,
                    ProductParser.class, ProductParserImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> objectClass = getImplementClass(interfaceClazz);
        Field[] fields = objectClass.getDeclaredFields();
        Object instanceObject = null;
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                instanceObject = createNewInstance(objectClass);
                field.setAccessible(true);
                try {
                    field.set(instanceObject, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't assign instance. Class: "
                            + objectClass.getName() + ". Field: " + field.getName(), e);
                }
            }
        }
        if (instanceObject == null) {
            instanceObject = createNewInstance(objectClass);
        }
        return instanceObject;
    }

    private Object createNewInstance(Class<?> objectClass) {
        if (!objectClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't create object of class "
            + objectClass.getName() + ". Component annotation is absent");
        }
        if (instances.containsKey(objectClass)) {
            return instances.get(objectClass);
        }
        try {
            Constructor<?> constructor = objectClass.getConstructor();
            Object object = constructor.newInstance();
            instances.put(objectClass, object);
            return object;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a instance of class "
                    + objectClass.getName(), e);
        }
    }

    private Class<?> getImplementClass(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return implementedClassesMap.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
