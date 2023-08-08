package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private static final Map<Class<?>, Class<?>> INTERFACE_IMPL = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductService.class, ProductServiceImpl.class,
            ProductParser.class, ProductParserImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClass) {
        Class<?> implementationClass = findImpl(interfaceClass);
        checkComponentAnnotation(implementationClass);
        Field[] declaredFields = implementationClass.getDeclaredFields();
        Object classImplInstance = createNewInstance(implementationClass);

        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.setAccessible(true);
                    field.set(classImplInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value "
                            + "Class: " + implementationClass.getName()
                            + ". Field: " + field.getName(), e);
                }
            }
        }

        return classImplInstance;
    }

    private void checkComponentAnnotation(Class<?> implementationClass) {
        if (!implementationClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can't get instance of class "
                    + implementationClass.getName()
                    + ". Class must have '@Component' annotation!!!");
        }
    }

    private Object createNewInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of " + clazz.getName(), e);
        }
    }

    private Class<?> findImpl(Class<?> interfaceClass) {
        return interfaceClass.isInterface() ? INTERFACE_IMPL.get(interfaceClass) : interfaceClass;
    }
}
