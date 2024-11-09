package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import mate.academy.NoComponentAnnotationException;
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
        Object clazzImlementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = interfaceClazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fielginstance = getInstance(field.getType());
                clazzImlementationInstance = createNewInstance(clazz);
            }
        }
        if (clazzImlementationInstance == null) {
            clazzImlementationInstance = createNewInstance(clazz);
        }
        return clazzImlementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        if (clazz.isAnnotationPresent(Component.class)) {
            Constructor<?> constructor = null;
            try {
                constructor = clazz.getConstructor();
                Object instance = constructor.newInstance();
                instances.put(clazz,instance);
                return instance;
            } catch (NoSuchMethodException | InvocationTargetException
                     | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("",e);
            }
        } else {
            throw new NoComponentAnnotationException("can't create class.N0 notification "
                    + "Component");
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>,Class<?>> imlementation = new HashMap<>();
        imlementation.put(ProductService.class, ProductServiceImpl.class);
        imlementation.put(ProductParser.class, ProductParserImpl.class);
        imlementation.put(FileReaderService.class, FileReaderServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return imlementation.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
