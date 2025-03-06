package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
    private final Map<Class<?>, Object> createdObjects = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Object interfaceClazzInstance = null;
        Field[] clazzFields = clazz.getDeclaredFields();
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Interface class " + interfaceClazz.getName()
                    + " is not annotated with @Component so you can't inject it");
        }
        for (Field clazzField : clazzFields) {
            if (clazzField.isAnnotationPresent(Inject.class)) {
                Object clazzFieldObject = getInstance(clazzField.getType());
                interfaceClazzInstance = createNewInstance(clazz);
                clazzField.setAccessible(true);
                try {
                    clazzField.set(interfaceClazzInstance, clazzFieldObject);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field "
                            + clazzField.getName()
                            + " of the class: " + clazz.getName(),e);
                }
            }
        }
        if (interfaceClazzInstance == null) {
            interfaceClazzInstance = createNewInstance(clazz);
        }
        return interfaceClazzInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (createdObjects.containsKey(clazz)) {
            return createdObjects.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object createdInstance = constructor.newInstance();
            createdObjects.put(clazz, createdInstance);
            return createdInstance;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                 | InvocationTargetException e) {
            throw new RuntimeException("Can't create new instance of " + clazz.getName(), e);
        }

    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceMap = new HashMap<Class<?>, Class<?>>();
        interfaceMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceMap.put(ProductParser.class, ProductParserImpl.class);
        interfaceMap.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return interfaceMap.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
