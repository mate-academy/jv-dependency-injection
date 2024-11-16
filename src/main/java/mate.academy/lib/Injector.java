package mate.academy.lib;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import mate.academy.exception.InjectionException;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private static final Map<Class<?>, Object> instanceMap = new HashMap<>();
    private static final Map<Class<?>, Class<?>> classImplMap = new HashMap<>();

    static {
        classImplMap.put(FileReaderService.class, FileReaderServiceImpl.class);
        classImplMap.put(ProductParser.class, ProductParserImpl.class);
        classImplMap.put(ProductService.class, ProductServiceImpl.class);
    }

    private Injector() {
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementationClazz = findImplementation(interfaceClazz);
        Object clazzImplementationInstance = createNewInstance(implementationClazz);

        Field[] fields = implementationClazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't set field to it's instance value ", e);
                }
            }
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> implementationClazz) {
        try {
            if (!instanceMap.containsKey(implementationClazz)) {
                if (!implementationClazz.isAnnotationPresent(Component.class)) {
                    throw new InjectionException("Class implementation should "
                            + "have annotation 'Component'");
                }
                Object instance = implementationClazz.getConstructor().newInstance();
                instanceMap.put(implementationClazz, instance);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create instance of class + "
                    + implementationClazz.getName(), e);
        }

        return instanceMap.get(implementationClazz);
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            return classImplMap.get(interfaceClazz);
        } else {
            return interfaceClazz;
        }
    }
}
