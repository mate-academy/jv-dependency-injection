package mate.academy.lib;

import mate.academy.model.Product;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();

    public static Injector getInjector() {
        return injector;
    }

    private Map<Class<?>, Object> instances = new HashMap<>();

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzImplementationinstance = null;
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                //create new object
                Object fieldInstance = getInstance(field.getType());
                //create new object interface
                clazzImplementationinstance = createInstance(field.getType());
                //set field
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationinstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field " + field.getName(), e);
                }
            }
        }
        if  (clazzImplementationinstance == null) {
            clazzImplementationinstance = createInstance(clazz);
        }
        return clazzImplementationinstance;
    }

    public Object createInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException | InvocationTargetException
                 | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Can't create instance of  " + clazz.getName(), e);
        }
    }

    private Class findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> implementationsInterfaces = new HashMap<>();
        implementationsInterfaces.put(ProductService.class, ProductServiceImpl.class);
        implementationsInterfaces.put(ProductParser.class, ProductParserImpl.class);
        implementationsInterfaces.put(FileReaderService.class, FileReaderServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return implementationsInterfaces.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
