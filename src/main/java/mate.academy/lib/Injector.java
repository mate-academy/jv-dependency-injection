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
    private Map<Class<?>,Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplInstance = null;
        Class<?> clazz = findImplement(interfaceClazz);
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplInstance = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplInstance,fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. "
                            + "Class " + clazz.getName() + ". Field " + field.getName(), e);
                }
            }
        }
        if (clazzImplInstance == null) {
            clazzImplInstance = createNewInstance(clazz);
        }
        return clazzImplInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz,instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of "
                    + clazz.getName(), e);
        }
    }

    private Class<?> findImplement(Class<?> interfaceClazz) {
        Class<?> findClass = null;
        Map<Class<?>,Class<?>> interfaceImpl = getMapInterfaceAndImplement();
        if (interfaceImpl.containsKey(interfaceClazz)) {
            findClass = interfaceImpl.get(interfaceClazz);
        } else if (interfaceImpl.containsValue(interfaceClazz)) {
            findClass = interfaceClazz;
        }
        isAnnotationComponentPresent(findClass);
        return findClass;
    }

    private Map<Class<?>,Class<?>> getMapInterfaceAndImplement() {
        Map<Class<?>,Class<?>> interfaceMapImpl = new HashMap<>();
        interfaceMapImpl.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceMapImpl.put(ProductParser.class, ProductParserImpl.class);
        interfaceMapImpl.put(ProductService.class, ProductServiceImpl.class);
        return interfaceMapImpl;
    }

    private Boolean isAnnotationComponentPresent(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Component.class)) {
            return true;
        }
        throw new RuntimeException("Before create instance class "
                + "should have @Component annotation " + clazz.getName());
    }
}
