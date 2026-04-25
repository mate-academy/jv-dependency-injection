package mate.academy.lib;

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

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implClass = findImplementation(interfaceClazz);

        if (implClass == null) {
            throw new RuntimeException("No implementation found for: "
                    + interfaceClazz.getName());
        }

        if (!implClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Class " + implClass.getName() + " is not a @Component");
        }

        try {
            Object instance = implClass.getDeclaredConstructor().newInstance();

            injectFields(instance, implClass);

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Can't create instance of: "
                    + implClass.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceToImpl = new HashMap<>();
        interfaceToImpl.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceToImpl.put(ProductParser.class, ProductParserImpl.class);
        interfaceToImpl.put(ProductService.class, ProductServiceImpl.class);
        return interfaceToImpl.get(interfaceClazz);
    }

    private void injectFields(Object instance, Class<?> clazz) throws Exception {
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object dependency = getInstance(field.getType());

                field.setAccessible(true);
                field.set(instance, dependency);
            }
        }
    }
}
