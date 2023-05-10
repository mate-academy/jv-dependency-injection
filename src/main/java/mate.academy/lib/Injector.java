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

    private Map<Class<?>, Object> instance = new HashMap();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object initialisationClass = null;
        Class<?> classImpl = findClassImpl(interfaceClazz);
        if (!classImpl.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(classImpl + " is class doesn't annotation Component");
        } else {
            Field[] declaredFields = classImpl.getDeclaredFields();
            for (Field fields : declaredFields) {
                if (fields.isAnnotationPresent(Inject.class)) {
                    Object instance1 = getInstance(fields.getType());
                    initialisationClass = createInitialisationClass(classImpl);
                    fields.setAccessible(true);
                    try {
                        fields.set(initialisationClass, instance1);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Can't initialize field value; Class : "
                                + classImpl.getName()
                                + "Filed : " + fields.getName());
                    }
                }
            }
        }
        if (initialisationClass == null) {
            return initialisationClass = createInitialisationClass(classImpl);
        }
        return initialisationClass;
    }

    private Object createInitialisationClass(Class<?> clazz) {
        if (instance.containsKey(clazz)) {
            return instance.get(clazz);
        }
        try {
            Object object = clazz.getDeclaredConstructor().newInstance();
            instance.put(clazz, object);
            return object;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("error initialization clazz " + clazz.getName());
        }
    }

    private Class<?> findClassImpl(Class<?> clazz) {
        Map<Class<?>, Class<?>> mapClass = new HashMap<>();
        mapClass.put(FileReaderService.class, FileReaderServiceImpl.class);
        mapClass.put(ProductParser.class, ProductParserImpl.class);
        mapClass.put(ProductService.class, ProductServiceImpl.class);
        if (clazz.isInterface()) {
            return mapClass.get(clazz);
        }
        return clazz;
    }
}
