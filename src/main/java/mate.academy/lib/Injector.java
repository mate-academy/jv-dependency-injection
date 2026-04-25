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

    private static final Map<Class<?>, Class<?>> interfaceToImpl = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implClass = interfaceToImpl.get(interfaceClazz);

        if (implClass == null) {
            throw new RuntimeException("No implementation found for: "
                    + interfaceClazz.getName());
        }

        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + implClass.getName() + " is not a @Component");
        }

        if (instances.containsKey(implClass)) {
            return instances.get(implClass);
        }

        try {
            Object instance = implClass.getDeclaredConstructor().newInstance();

            instances.put(implClass, instance);
            injectFields(instance, implClass);

            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of: "
                    + implClass.getName(), e);
        }
    }

    private void injectFields(Object instance, Class<?> clazz)
            throws ReflectiveOperationException {
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object dependency = getInstance(field.getType());

                field.setAccessible(true);
                field.set(instance, dependency);
            }
        }
    }
}
