package mate.academy.lib;

import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();

    public static Injector getInjector() {
        return injector;
    }

    private Map<Class<?>, Object> instances = new HashMap<>();


    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(Inject.class) && interfaceClazz.getClass().isAnnotationPresent(Component.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazz);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. " + "Class: "
                            + clazz.getName() + ". Field: " + field.getName());
                }
            }
        }
                if (clazzImplementationInstance == null) {
                    clazzImplementationInstance = createNewInstance(clazz);
                }
        return clazzImplementationInstance;
        }

        private Object createNewInstance (Class < ? > clazz) {

            if (instances.containsKey(clazz)) {
                return instances.getClass();
            }
            try {
                Constructor<?> constructor = clazz.getConstructor();
//            constructor.setAccessible(true);
                Object instance = constructor.newInstance();
                instances.put(clazz, instance);
                return instance;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Can't create a new instance of " + clazz.getName());
            }
        }

        private Class<?> findImplementation (Class < ? > interfaceClazz) {
            Map<Class<?>, Class<?>> interfaceImplementation = new HashMap<>();
            interfaceImplementation.put(FileReaderService.class, FileReaderServiceImpl.class);
            interfaceImplementation.put(ProductParser.class, ProductParserImpl.class);
            interfaceImplementation.put(ProductService.class, ProductServiceImpl.class);
            if (interfaceClazz.isInterface()) {
                return interfaceImplementation.get(interfaceClazz);
            }
            return interfaceClazz;
        }
    }

