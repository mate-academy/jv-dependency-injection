package mate.academy.lib;

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
    private final Map<Class<?>, Object> instances = new HashMap<>();

    private Injector() {
        instances.put(FileReaderService.class, new FileReaderServiceImpl());
        instances.put(ProductParser.class, new ProductParserImpl());
        instances.put(ProductService.class,
                new ProductServiceImpl(new ProductParserImpl(), new FileReaderServiceImpl()));
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Injection failed,"
                    + " missing @Component annotation on the class "
                    + clazz.getName());
        }

        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            injectDependencies(instance);
            instances.put(clazz, instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Can't initialize object with Injector "
                    + clazz.getName(), e);
        }
    }

    private void injectDependencies(Object targetInstance) {
        Field[] fields = targetInstance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                Class<?> fieldType = field.getType();

                try {
                    if (instances.containsKey(fieldType)) {
                        field.set(targetInstance, instances.get(fieldType));
                    } else {
                        Object fieldInstance = getInstance(fieldType);
                        field.set(targetInstance, fieldInstance);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error while injecting dependencies for field "
                            + field.getName()
                            + " in class "
                            + targetInstance.getClass().getName(), e);
                }
            }
        }
    }
}
