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
    private Map<Class<?>, Object> instaces = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) throws RuntimeException {
        Object classImplementationInstance = null;
        Class<?> clats = findImplementation(interfaceClazz);
        Field[] fields = clats.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                classImplementationInstance = createNewInstance(clats);
                try {
                    field.setAccessible(true);
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value " + field.getName()
                            + "Class " + clats.getName(), e);
                }
            }
        }
        if (clats.isAnnotationPresent(Component.class)) {
            classImplementationInstance = createNewInstance(clats);;
        } else {
            throw new RuntimeException("Injection failed, missing "
                    + "@Component annotaion on the class" + clats.getName());
        }
        return classImplementationInstance;
    }

    private Object createNewInstance(Class<?> clats) throws RuntimeException {
        if (instaces.containsKey(clats)) {
            return instaces.get(clats);
        }
        Constructor<?> conctructor = null;
        try {
            conctructor = clats.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        Object instance = null;
        try {
            instance = conctructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Object can`t be generated", e);
        }
        instaces.put(clats, instance);
        return instance;
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplamentation = new HashMap<>();
        interfaceImplamentation.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplamentation.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplamentation.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return interfaceImplamentation.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
