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
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> searchInterfaceImplMap = Map.of(FileReaderService.class,
            FileReaderServiceImpl.class, ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        //Create temp null Object
        Object clazzImplInst = null;
        //Create generic class to find Implementation in my classes
        Class<?> clazz = findImplementationInstance(interfaceClazz);
        //Check to - have annotations component class
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can`t create an instance at this class!");
        }

        Field[] declaredField = clazz.getDeclaredFields();

        for (Field field : declaredField) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object filedInstance = getInstance(field.getType());

                clazzImplInst = createNewIstance(clazz);

                field.setAccessible(true);
                try {
                    field.set(clazzImplInst, filedInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t initialize field value. Class"
                            + clazz.getName()
                            + " Field " + field.getName(), e);
                }
            }
        }
        if (clazzImplInst == null) {
            clazzImplInst = createNewIstance(clazz);
        }
        return clazzImplInst;
    }

    private Object createNewIstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        //Create generic constructor to
        Constructor<?> constructor;

        try {
            constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No such method." + e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Can`t invoke constructor of " + clazz.getName(), e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Can`t create new instance of " + clazz.getName(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Can`t reflective new instance of" + clazz.getName(), e);
        }
    }

    public Class<?> findImplementationInstance(Class<?> interfaceClass) {
        return searchInterfaceImplMap.get(interfaceClass);
    }
}
