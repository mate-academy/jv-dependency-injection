package mate.academy.lib;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] declaredFields = clazz.getDeclaredFields();

        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(declaredField.getType());

                clazzImplementationInstance = createNewInstance(clazz);

                declaredField.setAccessible(true);
                try {
                    declaredField.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. "
                            + "class: " + clazz.getName() + ".Field: "
                            + declaredField.getName(), e);
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException
                 | NoSuchMethodException e) {
            throw new RuntimeException("can't create new instance of: " + clazz.getName(), e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>();
        String packageName = "mate.academy.service.impl";
        String packagePath = packageName.replace('.', '/');

        File packageDirectory = new File(Thread.currentThread()
                .getContextClassLoader().getResource(packagePath).getFile());

        for (File file : packageDirectory.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".class")) {
                String className = packageName + "."
                        + file.getName().replace(".class", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
                        if (interfaceClazz.isAssignableFrom(clazz)) {
                            interfaceImplementations.put(interfaceClazz, clazz);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("there is no Implementations to this interface: "
                            + interfaceClazz, e);
                }
            }
        }
        return interfaceImplementations.get(interfaceClazz);
    }
}
