package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;
import org.reflections.Reflections;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplV2(interfaceClazz);
        isImplClassWithAnotation(clazz);
        Object clazzImplInstance = null;
        Field[] clazzDeclaratedFields = clazz.getDeclaredFields();
        for (Field clazzField : clazzDeclaratedFields) {
            if (clazzField.isAnnotationPresent(Inject.class)) {
                Object clazzFieldInstance = getInstance(clazzField.getType());
                clazzImplInstance = createNewInstance(clazz);
                clazzField.setAccessible(true);
                try {
                    clazzField.set(clazzImplInstance,clazzFieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value"
                            + " Class: " + clazz.getName()
                            + " Field: " + clazzField.getName(),e);
                }
            }
        }
        if (clazzImplInstance == null) {
            clazzImplInstance = createNewInstance(clazz);
        }
        return clazzImplInstance;
    }

    private boolean isImplClassWithAnotation(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed." + System.lineSeparator()
                    + "Can't create instance of : " + clazz.getName() + System.lineSeparator()
                    + "Because class haven't annotation: " + Component.class.getName());
        }
        return true;
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

    private Class<?> findImpl(Class<?> clazz) {
        Map<Class<?>, Class<?>> implClazzez = new HashMap<>();
        implClazzez.put(ProductService.class, ProductServiceImpl.class);
        implClazzez.put(ProductParser.class, ProductParserImpl.class);
        implClazzez.put(FileReaderService.class, FileReaderServiceImpl.class);
        if (clazz.isInterface()) {
            return implClazzez.get(clazz);
        }
        return clazz;
    }

    private Class<?> findImplV2(Class<?> clazz) {
        Reflections reflections = new Reflections("mate.academy");
        Set<Class<?>> classes = (Set<Class<?>>) reflections.getSubTypesOf(clazz);
        if (clazz.isInterface()) {
            return classes.iterator().next();
        }
        return clazz;
    }
}
