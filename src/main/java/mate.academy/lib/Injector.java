package mate.academy.lib;

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
        Class<?> cls = getImpl(interfaceClazz);
        if (!cls.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed, "
                    + "missing @Component annotation on the class: " + cls.getName());
        }

        Object clsInstance = null;
        Field[] declaredFields = cls.getDeclaredFields();

        for (Field field: declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                // create service
                Object serviceInstance = getInstance(field.getType());;
                // create object
                clsInstance = getPresentInstanceOrCreateNew(cls);
                // set service as a field
                try {
                    field.setAccessible(true);
                    field.set(clsInstance, serviceInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't set field "
                            + field.getName()
                            + " for " + clsInstance.getClass().getName() + " instance");
                }
            }
        }

        if (clsInstance == null) {
            clsInstance = getPresentInstanceOrCreateNew(cls);
        }
        return clsInstance;
    }

    private Class<?> getImpl(Class<?> cls) {
        Map<Class<?>, Class<?>> instances = new HashMap<>();
        instances.put(FileReaderService.class, FileReaderServiceImpl.class);
        instances.put(ProductParser.class, ProductParserImpl.class);
        instances.put(ProductService.class, ProductServiceImpl.class);
        if (cls.isInterface()) {
            return instances.get(cls);
        }
        return cls;
    }

    private Object getPresentInstanceOrCreateNew(Class<?> cls) {
        if (instancesMap.containsKey(cls)) {
            return instancesMap.get(cls);
        }
        try {
            Object instance = cls.getConstructor().newInstance();
            instancesMap.put(cls, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create new instance of " + cls.getName(), e);
        }
    }
}
