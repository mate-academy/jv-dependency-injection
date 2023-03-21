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
    private Map<Class<?>, Class<?>> interfaceImplementations = Map.of(
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            FileReaderService.class, FileReaderServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClass) {
        Class<?> instanceClass = findImplementation(interfaceClass);
        if (!instanceClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + instanceClass.getName()
                    + " doesn't have annotation " + Component.class.getName());
        }
        Object classImplamentationsIntance = null;
        Field[] declaredClassFields = instanceClass.getDeclaredFields();
        for (Field field : declaredClassFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldClassInstance = getInstance(field.getType());
                classImplamentationsIntance = createNewInstance(instanceClass);
                try {
                    field.setAccessible(true);
                    field.set(classImplamentationsIntance, fieldClassInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialise field value. Class "
                            + instanceClass.getName() + ". Field " + field.getName());
                } catch (SecurityException e) {
                    System.out.println("Accessibility of field "
                            + field.getName() + " not be changed");
                }
            }
        }
        if (classImplamentationsIntance == null) {
            classImplamentationsIntance = createNewInstance(instanceClass);
        }
        return classImplamentationsIntance;
    }

    private Object createNewInstance(Class<?> classInstance) {
        if (instances.containsKey(classInstance)) {
            return instances.get(classInstance);
        }
        try {
            Constructor<?> constructor = classInstance.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(classInstance, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance " + classInstance.getName());
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClass) {
        if (interfaceClass.isInterface()) {
            return interfaceImplementations.get(interfaceClass);
        }
        return interfaceClass;
    }
}
