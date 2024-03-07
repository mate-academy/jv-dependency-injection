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
    private Map<Class<?>, Class<?>> implementationsMap;
    private final Map<Class<?>, Object> createdInstances = new HashMap<>();

    private Injector() {
        initImplementationsMap();
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = getClassImplementation(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't create the instance of the class: "
                    + clazz.getName() + "The class is not annotated with @Component");
        }
        Object clazzInstance = createNewClassInstance(clazz);
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(clazzInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't access field: " + field.getName()
                            + " of class: " + clazz.getName());
                }
            }
        }
        return clazzInstance;
    }

    private Object createNewClassInstance(Class<?> clazz) {
        if (createdInstances.containsKey(clazz)) {
            return createdInstances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object clazzInstance = constructor.newInstance();
            createdInstances.put(clazz, clazzInstance);
            return clazzInstance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> getClassImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return implementationsMap.get(interfaceClazz);
        }
        return interfaceClazz;
    }

    private void initImplementationsMap() {
        implementationsMap = Map.of(FileReaderService.class, FileReaderServiceImpl.class,
                ProductService.class, ProductServiceImpl.class,
                ProductParser.class, ProductParserImpl.class);
    }
}
