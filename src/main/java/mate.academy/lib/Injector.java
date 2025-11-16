package mate.academy.lib;

import java.lang.reflect.Constructor;
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
public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> implementationMap = new HashMap<>();

    public Injector() {
        implementationMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementationMap.put(ProductParser.class, ProductParserImpl.class);
        implementationMap.put(ProductService.class, ProductServiceImpl.class);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceOrComponentType) {
        Class<?> implementationClazz = findImplementation(interfaceOrComponentType);
        if (!implementationClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Injection failed, missing @Component annotation on the class "
                            + implementationClazz.getName());
        }
        Object instance = createNewInstance(implementationClazz);
        Field[] fields = implementationClazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object dependencyInstance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(instance, dependencyInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot set injected field " + field.getName(), e);
                }
            }
        }

        return instance;
    }

    private Class<?> findImplementation(Class<?> clazzImplementationInstance) {
        if (clazzImplementationInstance.isInterface()) {
            return implementationMap.get(clazzImplementationInstance);
        } else {
            return clazzImplementationInstance;
        }
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object object = constructor.newInstance();
            instances.put(clazz, object);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("can't create new instance", e);
        }
        return instances.get(clazz);
    }
}
