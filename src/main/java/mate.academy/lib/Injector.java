package mate.academy.lib;

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
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
       Object classImplementationInstanse = null;
       Class<?> clazz = findImplementation(interfaceClazz);
       if (!clazz.isAnnotationPresent(Component.class)) {
           throw new RuntimeException("Class doesn't annoted Component!");
       }
       if (instances.containsKey(clazz)) {
           return instances.get(clazz);
       }

       try {
           Constructor<?> constructor = clazz.getConstructor();
           classImplementationInstanse = constructor.newInstance();
           instances.put(clazz, classImplementationInstanse);
           Field[] declaredFields = clazz.getDeclaredFields();
           for (Field field : declaredFields) {
               if (field.isAnnotationPresent(Inject.class)) {
                   Object fieldInstans = getInstance(field.getType());
                   field.setAccessible(true);
                   field.set(classImplementationInstanse, fieldInstans);
               }
           }

       } catch (NoSuchMethodException | InstantiationException
                | IllegalAccessException
                | InvocationTargetException e) {
           throw new RuntimeException("Can not create new instance :" + e);
       }
       return classImplementationInstanse;

    }

    public Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> mapImpl = Map.of(
                FileReaderService.class, FileReaderServiceImpl.class,
                ProductParser.class, ProductParserImpl.class,
                ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return mapImpl.get(interfaceClazz);
        } else {
            return interfaceClazz;
        }
    }
}
