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

public class Injector {
    private static final Injector injector = new Injector();

    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (!interfaceClazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "class " + interfaceClazz.getName() + " is not a component"
            );
        }

        Class<?> implementationClazz = findImplementation(interfaceClazz);
        Object instanceOfImplClazz = null;
        Field[] fields = implementationClazz.getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                instanceOfImplClazz = createInstance(implementationClazz);
                try {
                    field.setAccessible(true);
                    field.set(instanceOfImplClazz, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                            "cant initialize field value," + System.lineSeparator()
                            + "class: " + interfaceClazz.getName() + ", field: " + field.getName()
                    );
                }
            }
        }
        if (instanceOfImplClazz == null) {
            instanceOfImplClazz = createInstance(implementationClazz);
        }
        return instanceOfImplClazz;
    }

    private Object createInstance(Class<?> clazzImpl) {
        if (instances.containsKey(clazzImpl)) {
            return instances.get(clazzImpl);
        }
        Object instance;
        try {
            Constructor<?> constructor = clazzImpl.getConstructor();
            instance = constructor.newInstance();
            instances.put(clazzImpl, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("cant create new Instance of: " + clazzImpl.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> implementationMap = new HashMap<>();
        implementationMap.put(ProductParser.class, ProductParserImpl.class);
        implementationMap.put(ProductService.class, ProductServiceImpl.class);
        implementationMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        return implementationMap.getOrDefault(interfaceClazz, interfaceClazz);
    }
}
