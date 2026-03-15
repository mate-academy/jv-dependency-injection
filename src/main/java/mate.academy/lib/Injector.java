package mate.academy.lib;

import java.lang.reflect.Field;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementationClass = findImplementation(interfaceClazz);

        if (!implementationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class is not annotated with @Component");
        }

        try {
            Object instance = implementationClass.getDeclaredConstructor().newInstance();

            for (Field field : implementationClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstance = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(instance, fieldInstance);
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance", e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz == ProductService.class) {
            return ProductServiceImpl.class;
        }
        if (interfaceClazz == ProductParser.class) {
            return ProductParserImpl.class;
        }
        if (interfaceClazz == FileReaderService.class) {
            return FileReaderServiceImpl.class;
        }
        throw new RuntimeException("No implementation found");
    }
}

