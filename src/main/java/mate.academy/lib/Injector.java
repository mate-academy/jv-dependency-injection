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
    private static final Map<Class<?>, Class<?>> IMPLEMENTATIONS = Map.of(
            ProductService.class, ProductServiceImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class
    );

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (!interfaceClazz.isAnnotationPresent(Component.class) && !interfaceClazz.isInterface()) {
            throw new RuntimeException(interfaceClazz.getName() 
                    + " is not annotated with @Component");
        }
        
        Class<?> clazz = getImplementation(interfaceClazz);
        Field[] fields = clazz.getDeclaredFields();
        Object instance = createNewInstance(clazz);
        
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object dependency = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(instance, dependency);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Unable to inject field " + field.getName(), e);
                }
            }
        }
        return instance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (IMPLEMENTATIONS.containsKey(clazz)) {
            try {
                return IMPLEMENTATIONS.get(clazz).getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Unable to inject field " + clazz.getName(), e);
            }
        }
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of class: " + clazz.getName(), e);
        }
    }

    private Class<?> getImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return IMPLEMENTATIONS.get(interfaceClazz);
        }
        return interfaceClazz;
    }
}
