package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        try {
            if (instances.containsKey(interfaceClazz)) {
                return instances.get(interfaceClazz);
            }

            Class<?> implClazz = findImplementation(interfaceClazz);
            if (!implClazz.isAnnotationPresent(Component.class)) {
                throw new RuntimeException("Class " + implClazz.getName()
                        + " is not annotated with @Component");
            }

            Object implInstance = implClazz.getDeclaredConstructor().newInstance();
            Field[] fields = implClazz.getDeclaredFields();

            for (Field field : fields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Class<?> fieldType = field.getType();
                    Object fieldInstance = getInstance(fieldType);
                    field.setAccessible(true);
                    field.set(implInstance, fieldInstance);
                }
            }

            instances.put(interfaceClazz, implInstance);
            return implInstance;
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of class: "
                    + interfaceClazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            if (interfaceClazz.equals(mate.academy.service.ProductService.class)) {
                return mate.academy.service.impl.ProductServiceImpl.class;
            } else if (interfaceClazz.equals(mate.academy.service.ProductParser.class)) {
                return mate.academy.service.impl.ProductParserImpl.class;
            } else if (interfaceClazz.equals(mate.academy.service.FileReaderService.class)) {
                return mate.academy.service.impl.FileReaderServiceImpl.class;
            }
        }
        throw new RuntimeException("No implementation found for interface: "
                + interfaceClazz.getName());
    }
}
