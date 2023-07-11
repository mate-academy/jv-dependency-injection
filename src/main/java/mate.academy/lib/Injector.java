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
    private final Map<Class<?>, Object> instances;

    private Injector() {
        instances = new HashMap<>();
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        if (!interfaceClazz.isInterface()) {
            throw new RuntimeException("Неподдерживаемый класс: " + interfaceClazz.getName());
        }

        if (instances.containsKey(interfaceClazz)) {
            return instances.get(interfaceClazz);
        }

        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzImplementationInstance = createNewInstance(clazz);
        initializeFields(clazz, clazzImplementationInstance);

        instances.put(interfaceClazz, clazzImplementationInstance);

        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Невозможно создать новый экземпляр класса "
                    + clazz.getName());
        }
    }

    private void initializeFields(Class<?> clazz, Object instance) {
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                Object fieldInstance = getInstance(field.getType());
                try {
                    field.set(instance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Невозможно инициализировать значение поля. "
                            + "Класс: " + clazz.getName() + ", Поле: " + field.getName());
                }
            }
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz == FileReaderService.class) {
            return FileReaderServiceImpl.class;
        } else if (interfaceClazz == ProductParser.class) {
            return ProductParserImpl.class;
        } else if (interfaceClazz == ProductService.class) {
            return ProductServiceImpl.class;
        } else {
            throw new RuntimeException("Не найдено реализации для интерфейса: "
                    + interfaceClazz.getName());
        }
    }
}
