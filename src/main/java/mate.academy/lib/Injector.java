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
    private static final Map<Class<?>, Class<?>> interfaceClass = new HashMap<>();

    static {
        interfaceClass.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceClass.put(ProductParser.class, ProductParserImpl.class);
        interfaceClass.put(ProductService.class, ProductServiceImpl.class);
    }

    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public <T> T getInstance(Class<T> interfaceClazz) {
        if (!interfaceClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + interfaceClazz.getName()
                + " doesn't have @Component");
        }
        if (instances.containsKey(interfaceClazz)) {
            return (T) instances.get(interfaceClazz);
        }
        try {
            Class<?> implementationClass = interfaceClass.get(interfaceClazz);
            if (implementationClass == null) {
                throw new RuntimeException(
                    "No implementation found for " + interfaceClazz.getName());
            }
            Object instance = implementationClass.getDeclaredConstructor().newInstance();
            instances.put(interfaceClazz, instance);
            Field[] fields = implementationClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Class<?> fieldType = field.getType();
                    T fieldInstance = (T) getInstance(fieldType); // Рекурсивно создаем зависимости
                    field.setAccessible(true);
                    field.set(instance, fieldInstance);
                }
            }
            return (T) instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                "Failed to create an instance of " + interfaceClazz.getName(), e);
        }
    }
}
