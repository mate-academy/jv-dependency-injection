package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import mate.academy.exceptions.MissingComponentAnnotationException;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();

    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);

        if (clazz.isAnnotationPresent(Component.class)) {
            Field[] declaredFields = clazz.getDeclaredFields();

            Object clazzImplInstance = null;

            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());

                    clazzImplInstance = createNewInstance(clazz);

                    try {
                        field.setAccessible(true);
                        field.set(clazzImplInstance, fieldInstance);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Can't initialize field in Class "
                                + clazz.getName()
                                + "Field name: "
                                + field.getName()
                        );
                    }
                }
            }
            if (clazzImplInstance == null) {
                clazzImplInstance = createNewInstance(clazz);
            }
            return clazzImplInstance;
        }
        throw new MissingComponentAnnotationException(
                "Missing @Component above " + clazz.getName() + " class."
        );
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
        } catch (NoSuchMethodException
                 | InstantiationException
                 | IllegalAccessException
                 | InvocationTargetException e) {
            throw new RuntimeException("Can't create new Instance of " + clazz.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplMap = new HashMap<>();
        interfaceImplMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplMap.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplMap.put(ProductService.class, ProductServiceImpl.class);

        return interfaceClazz.isInterface() ? interfaceImplMap.get(interfaceClazz) : interfaceClazz;
    }
}
