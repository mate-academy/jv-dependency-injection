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
    private Map<Class<?>, Object> services = new HashMap<>();
    private Map<Class<?>, Class<?>> implementationsMap = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    Injector() {
        implementationsMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementationsMap.put(ProductParser.class, ProductParserImpl.class);
        implementationsMap.put(ProductService.class, ProductServiceImpl.class);
    }

    public Object getInstance(Class<?> interfaceClazz) {


        Class<?> clazz = findImplementationClass(interfaceClazz);
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("No Component annotation found for "
                    + interfaceClazz.getName());
        }
        Field[] declaredFields = clazz.getDeclaredFields();
        Object fieldInstance = null;
        Object clazzImplementationInstance = createNewInstance(clazz);
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                fieldInstance = getInstance(field.getType());

                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't intitialize fueld value. Class "
                            + clazz.getName() + ". Field " + field.getName(), e);
                }
            }

        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {

        if (services.containsKey(clazz)) {
            return services.get(clazz);
        }

        Constructor<?> constructor = null;

        try {
            constructor = clazz.getConstructor();
            Object object = null;
            object = constructor.newInstance();
            services.put(clazz, object);
            return object;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of "
                    + clazz.getName(), e);
        }
    }

    private Class<?> findImplementationClass(Class<?> interfaceClazz) {

        if (interfaceClazz.isInterface()) {
            return implementationsMap.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}




