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

@Component
@SuppressWarnings("unchecked")
public class Injector {
    private static final Injector injector = new Injector();

    private final Map<Class<?>, Object> instancesMap = new HashMap<>();

    private final Map<Class<?>, Class<?>> implementationMap = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class
    );

    public static Injector getInjector() {
        return injector;
    }

    public <T> T getInstance(Class<T> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);

        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("The class " + clazz.getName()
                    + " is not annotated with @Component and cannot be instantiated");
        }

        if (instancesMap.containsKey(clazz)) {
            return (T) instancesMap.get(clazz);
        }

        Object clazzImplementationInstance = createNewInstance(clazz);
        instancesMap.put(clazz, clazzImplementationInstance);

        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Can't initialize field value. "
                            + "Class: " + clazz.getName() + ". Field: " + field.getName(), e);
                }
            }
        }

        return (T) clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create an instance of class: " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Class<?> implementation = implementationMap.get(interfaceClazz);
        if (implementation == null) {
            throw new RuntimeException("No implementation found for: " + interfaceClazz.getName());
        }
        return implementation;
    }
}
