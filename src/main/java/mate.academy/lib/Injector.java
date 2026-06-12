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
    private final Map<Class<?>, Class<?>> classImplementations = new HashMap<>();
    private final Map<Class<?>, Object> classInstances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (interfaceClazz == null) {
            throw new RuntimeException("Interface class cannot be null");
        }

        Class<?> implementationClass = getClazzImplementation(interfaceClazz);
        if (!implementationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Implementation class " + implementationClass.getName()
                    + " is not annotated with @Component");
        }

        Object classImplementationInstance = null;
        Field[] fields = implementationClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());

                classImplementationInstance = getInstanceOfImplClass(implementationClass);
                field.setAccessible(true);
                try {
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error while setting field " + field.getName()
                            + " for class " + interfaceClazz.getName(), e);
                }
            }
        }

        if (classImplementationInstance == null) {
            classImplementationInstance = getInstanceOfImplClass(implementationClass);
        }

        return classImplementationInstance;
    }

    private Object getInstanceOfImplClass(Class<?> clazz) {
        if (classInstances.containsKey(clazz)) {
            return classInstances.get(clazz);
        }

        Constructor<?>[] constructors = clazz.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                try {
                    Object instance = constructor.newInstance();
                    classInstances.put(clazz, instance);
                    return instance;
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Error while instantiating " + clazz.getName(), e);
                }
            }
        }

        throw new RuntimeException("Not found empty constructor for " + clazz.getName());
    }

    private Class<?> getClazzImplementation(Class<?> clazz) {
        if (!clazz.isInterface()) {
            return clazz;
        }
        if (classImplementations.isEmpty()) {
            fillClassImplementationsMap();
        }

        if (!classImplementations.containsKey(clazz)) {
            throw new RuntimeException("Not found class implementation for " + clazz);
        }

        return classImplementations.get(clazz);
    }

    private void fillClassImplementationsMap() {
        classImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        classImplementations.put(ProductService.class, ProductServiceImpl.class);
        classImplementations.put(ProductParser.class, ProductParserImpl.class);
    }
}
