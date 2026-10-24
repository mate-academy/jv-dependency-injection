package mate.academy.lib;

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
    private final Map<Class<?>, Class<?>> injectorMap = Map.of(
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = injectorMap.get(interfaceClazz);

        if (clazz == null) {
            throw new RuntimeException("Can't find implementation for "
                    + interfaceClazz.getName());
        }

        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't find annotation for "
                    + clazz.getName());
        }

        Object instance = null;

        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of class: "
                    + clazz.getName(), e);
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Inject.class)) {
                Class<?> type = field.getType();
                Object dependency = getInstance(type);
                try {
                    field.set(instance, dependency);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                            "Can't create instance of class: "
                                    + clazz.getName(), e);
                }
            }
        }

        return instance;
    }
}
