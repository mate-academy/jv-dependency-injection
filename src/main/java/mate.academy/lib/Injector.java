package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
        return getInstance(interfaceClazz, new HashSet<>());
    }

    private Object getInstance(Class<?> interfaceClazz, Set<Class<?>> instantiationStack) {
        Class<?> clazz = findImplementation(interfaceClazz);

        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Class " + clazz.getName()
                    + " is not annotated with @Component or no implementation found"
            );
        }

        if (instantiationStack.contains(clazz)) {
            throw new RuntimeException(
                    "Circular dependency detected for class: " + clazz.getName()
            );
        }

        instantiationStack.add(clazz);

        Object clazzImplementationInstance = createNewInstance(clazz);

        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType(), instantiationStack);

                try {
                    field.setAccessible(true);
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                            "Can't initialize field value. "
                            + "Class: " + clazz.getName() + ". Field: " + field.getName()
                    );
                }
            }
        }

        instantiationStack.remove(clazz);

        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of class: " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceToImplementation = new HashMap<>();
        interfaceToImplementation.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceToImplementation.put(ProductParser.class, ProductParserImpl.class);
        interfaceToImplementation.put(ProductService.class, ProductServiceImpl.class);

        Class<?> implementationClass =
                interfaceToImplementation.get(interfaceClazz);
        if (implementationClass == null) {
            throw new RuntimeException(
                    "No implementation found for interface: " + interfaceClazz.getName()
            );
        }
        return implementationClass;
    }
}
