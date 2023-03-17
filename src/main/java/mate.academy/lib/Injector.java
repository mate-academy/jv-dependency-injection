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
    private Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClass) {
        Class<?> currentClass = findImplementation(interfaceClass);
        if (!currentClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't get instance of class "
                    + interfaceClass.getClass());
        }
        Object classImplementationInstance = null;
        Field[] declaredFields = currentClass.getDeclaredFields();
        for (Field field : declaredFields) {
            // create a new Object of field Type
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());

                // create an Object of interfacaClass (or implementation class)
                classImplementationInstance = createNewInstance(currentClass);

                // set 'field type object' to 'interfaceClass object'
                field.setAccessible(true);
                try {
                    field.set(classImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class: "
                            + currentClass.getName() + ". Field: " + field.getName());
                }
            }
        }
        if (classImplementationInstance == null) {
            classImplementationInstance = createNewInstance(currentClass);

        }
        return classImplementationInstance;
    }

    private Object createNewInstance(Class<?> currentClass) {
        if (instances.containsKey(currentClass)) {
            return instances.get(currentClass);
        }

        Constructor<?> constructor;
        try {
            constructor = currentClass.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(currentClass, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create new instance of " + currentClass.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClass) {
        Map<Class<?>, Class<?>> interfaceImplamentations = new HashMap<>();
        interfaceImplamentations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplamentations.put(ProductParser.class, ProductParserImpl.class);
        interfaceImplamentations.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClass.isInterface()) {
            return interfaceImplamentations.get(interfaceClass);
        }
        return interfaceClass;
    }
}
