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
    private Map<Class<?>, Class<?>> implementations = Map.of(
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implClass = interfaceClazz;
        if (interfaceClazz.isInterface()) {
            implClass = implementations.get(interfaceClazz);
        }
        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + implClass.getName()
                    + " doesn't have @Component annotation");
        }
        try {
            Object instance = implClass.getDeclaredConstructor().newInstance();
            for (Field field : implClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Class<?> dependencyType = field.getType();
                    Object dependency = getInstance(dependencyType);
                    field.setAccessible(true);
                    field.set(instance, dependency);
                }
            }
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to instantiate "
                    + implClass.getName(), e);
        }
    }
}
