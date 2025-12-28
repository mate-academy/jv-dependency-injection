package mate.academy.lib;

import java.io.FileReader;
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

    private final Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {

        Class<?> clazz = interfaceClazz.isInterface()
                ? interfaceImplementations.get(interfaceClazz)
                : interfaceClazz;

        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Klasa " + clazz.getName() + "niema adnotacji @Component");
        }

        Object instance = null;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Nie udało sie znależć obiektu klasy " + clazz.getName(), e);
        }

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                field.setAccessible(true);

                try {
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Nie udało sie wstrzyknąć zależności!");
                }
            }
        }
        return instance;
    }
}
