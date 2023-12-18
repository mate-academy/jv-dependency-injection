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
    private static final Map<Class<?>, Class<?>> IMPLEMENTATIONS_MAP =
            Map.of(ProductService.class, ProductServiceImpl.class,
                    ProductParser.class, ProductParserImpl.class,
                    FileReaderService.class, FileReaderServiceImpl.class);
    private static Map<Class<?>, Object> createdClasses = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzImplementationInst = null;
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field fieldOfClass : declaredFields) {
            if (fieldOfClass.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(fieldOfClass.getType());
                clazzImplementationInst = createNewInst(clazz);
                try {
                    fieldOfClass.setAccessible(true);
                    fieldOfClass.set(clazzImplementationInst, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't assign fieldOfClass: "
                            + fieldOfClass.getName() + " to Class: " + clazz.getName(), e);
                }
            }
        }
        if (clazzImplementationInst == null) {
            clazzImplementationInst = createNewInst(clazz);
        }
        return clazzImplementationInst;
    }

    private Object createNewInst(Class<?> clazzImplementationInst) {
        if (createdClasses.containsKey(clazzImplementationInst)) {
            return createdClasses.get(clazzImplementationInst);
        }
        try {
            Constructor<?> constructor = clazzImplementationInst.getConstructor();
            Object instance = constructor.newInstance();
            createdClasses.put(clazzImplementationInst, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of class: "
                    + clazzImplementationInst.getName(), e);
        }

    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Class<?> implementationClazz;
        if (interfaceClazz.isInterface()) {
            implementationClazz = IMPLEMENTATIONS_MAP.get(interfaceClazz);
        } else {
            implementationClazz = interfaceClazz;
        }
        if (implementationClazz == null
                || !implementationClazz.isAnnotationPresent(Component.class)) {
            throw new UnsupportedOperationException("Unsupported class: " + implementationClazz
                    + " add @Component annotation to class and/or add it to hashMap");
        }
        return implementationClazz;
    }
}
