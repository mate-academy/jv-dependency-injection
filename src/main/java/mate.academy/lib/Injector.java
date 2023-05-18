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

    public static Injector getInjector() {
        return injector;
    }

    private Map<Class<?>, Object> instanсes = new HashMap<>();

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't initialize field, "
                    + "this class hasn't annotation Component. Class: " + clazz.getName());
        }
        Object clazzImplInstance = null;
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplInstance = createNewInstance(clazz);
                try {
                    field.setAccessible(true);
                    field.set(clazzImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. "
                            + "Class: " + clazz.getName() + "Field: " + field.getName());
                }
            }
        }
        if (clazzImplInstance == null) {
            clazzImplInstance = createNewInstance(clazz);
        }
        return clazzImplInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getConstructor();
            if (!clazz.isAnnotationPresent(Component.class)) {
                throw new RuntimeException("Can't initialize field, "
                        + "this class haven' annotation Component. Class: " + clazz.getName());
            }
            Object newInstance = constructor.newInstance();
            instanсes.put(clazz, newInstance);
            return newInstance;
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException
                 | NoSuchMethodException e) {
            throw new RuntimeException("Can't create instance of" + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplemetations = new HashMap<>();
        interfaceImplemetations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplemetations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplemetations.put(ProductService.class, ProductServiceImpl.class);
        return interfaceImplemetations.get(interfaceClazz);
    }
}
