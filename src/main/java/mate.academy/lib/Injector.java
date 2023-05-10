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
    private Map<Class<?>, Object> instancesMap = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clasImplementation = null;
        Class<?> clas = findImplementation(interfaceClazz);
        Field[] fields = clas.getDeclaredFields();
        if (!clas.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(
                    "Failed injection" + interfaceClazz);
        }
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object instance = getInstance(field.getType());
                clasImplementation = createInstance(clas);
                field.setAccessible(true);
                try {
                    field.set(clasImplementation, instance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field: " + field.getName(),e);
                }
            }
        }
        if (clasImplementation == null) {
            clasImplementation = createInstance(clas);
        }
        return clasImplementation;
    }

    private Object createInstance(Class<?> clas) {
        if (instancesMap.containsKey(clas)) {
            return instancesMap.get(clas);
        }
        try {
            Constructor<?> constructor = clas.getConstructor();
            Object instance = constructor.newInstance();
            instancesMap.put(clas, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create a new instance of class", e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClas) {
        Map<Class<?>, Class<?>> implementationsMap = new HashMap<>();
        implementationsMap.put(ProductParser.class, ProductParserImpl.class);
        implementationsMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        implementationsMap.put(ProductService.class, ProductServiceImpl.class);
        if (interfaceClas.isInterface()) {
            return implementationsMap.get(interfaceClas);
        }
        return interfaceClas;
    }
}
