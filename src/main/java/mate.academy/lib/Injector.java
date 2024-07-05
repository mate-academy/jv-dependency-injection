package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private static final Map<Class<?>, Class<?>> implementations = Map.of(
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        if (!interfaceClazz.isAssignableFrom(clazz)) {
            throw new RuntimeException("Implementation class " + clazz.getName()
                    + " does not implement " + interfaceClazz.getName());
        }
        Object clazzImplementationInstance = createNewInstance(clazz);
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = createNewInstance(findImplementation(field.getType()));
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Csn`t initialize field: " + field.getName(), e);
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Component.class)) {
            try {
                Constructor<?> constructor = clazz.getConstructor();
                return constructor.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Can`t create new instance of " + clazz.getName(), e);
            }
        } else {
            throw new RuntimeException("Can`t create an instance of the class: " + clazz.getName()
                    + "There is no @Component annotation");
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return implementations.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
