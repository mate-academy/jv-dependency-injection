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
    private static final Map<Class<?>, Class<?>> IMPLEMENTATIONS = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );
    private static final Injector INSTANCE = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return INSTANCE;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }
        try {
            Class<?> implementationClass = IMPLEMENTATIONS.get(interfaceClazz);
            if (implementationClass == null) {
                throw new RuntimeException(
                        "No implementation found for " + interfaceClazz.getName()
                );
            }
            if (!implementationClass.isAnnotationPresent(Component.class)) {
                throw new RuntimeException(
                        "Class " + implementationClass.getName()
                                + " is not marked with @Component"
                );
            }
            Object object = implementationClass
                    .getDeclaredConstructor()
                    .newInstance();
            instances.put(interfaceClazz, object);

            Field[] fields = implementationClass.getDeclaredFields();

            for (Field field : fields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object dependency = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(object, dependency);
                }
            }
            return object;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Can't create implementation for " + interfaceClazz.getName(), e
            );
        }
    }
}
