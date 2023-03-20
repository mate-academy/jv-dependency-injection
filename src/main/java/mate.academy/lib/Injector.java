package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
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
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> implClazzez = Map.of(
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = interfaceClazz.isInterface()
                         ? implClazzez.get(interfaceClazz) : interfaceClazz;
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed." + System.lineSeparator()
                    + "Can't create instance of : " + clazz.getName() + System.lineSeparator()
                    + "Because class haven't annotation: " + Component.class.getName());
        }
        Object clazzImplInstance = null;
        Field[] clazzDeclaratedFields = clazz.getDeclaredFields();
        for (Field clazzField : clazzDeclaratedFields) {
            if (clazzField.isAnnotationPresent(Inject.class)) {
                Object clazzFieldInstance = getInstance(clazzField.getType());
                clazzImplInstance = createNewInstance(clazz);
                try {
                    clazzField.setAccessible(true);
                    clazzField.set(clazzImplInstance,clazzFieldInstance);
                } catch (IllegalAccessException | InaccessibleObjectException e) {
                    throw new RuntimeException("Can't initialize field value"
                            + " Class: " + clazz.getName()
                            + " Field: " + clazzField.getName(),e);
                }
            }
        }
        return clazzImplInstance == null ? createNewInstance(clazz) : clazzImplInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of : " + clazz.getName(),e);
        }
    }
}
