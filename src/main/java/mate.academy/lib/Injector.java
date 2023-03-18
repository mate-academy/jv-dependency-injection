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
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = getImplementation(interfaceClazz);
        Object classImplementationInctance = null;
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't create an instance of this class " + clazz.getName());
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                // we need to initialize this field
                Object fieldInstance = getInstance(field.getType());
                // create a new object
                classImplementationInctance = createNewInstance(clazz);
                // create a new object of interface or impl class
                field.setAccessible(true);
                try {
                    field.set(classImplementationInctance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value, Class "
                            + clazz.getName() + "field " + field.getName());
                }
                // set field type object to intarfeceClass object
            }
        }
        if (classImplementationInctance == null) {
            classImplementationInctance = createNewInstance(clazz);
        }
        return classImplementationInctance;
    }

    private Object createNewInstance(Class<?> clazz) {
        // if we crate an object let's use it
        if (instances.containsKey(clazz)) {
            instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                 | IllegalAccessException e) {
            throw new RuntimeException("Can't create instance of Class " + clazz.getName());
        }
    }

    private Class<?> getImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplMap = new HashMap<>();
        interfaceImplMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplMap.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplMap.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClazz.isInterface()) {
            return interfaceImplMap.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
