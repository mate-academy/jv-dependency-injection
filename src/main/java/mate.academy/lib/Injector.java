package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Injector {
    private static final String SERVICE_INTERFACE_PACKAGE =
            "mate.academy.service";
    private static final String SERVICE_INSTANCE_PACKAGE =
            "mate.academy.service.impl";
    private static Map<Class<?>, Object> instances;
    private static Map<Class<?>, Class<?>> interfaceImplementationMap;
    private static Injector injector;

    {
        instances = new HashMap<>();
        AccessingAllClassesInPackage accessingAllClassesInPackage =
                new AccessingAllClassesInPackage();
        List<Class<?>> interfacesList = accessingAllClassesInPackage
                .findAllClassesUsingClassLoader(SERVICE_INTERFACE_PACKAGE);
        List<Class<?>> implementsList = accessingAllClassesInPackage
                .findAllClassesUsingClassLoader(SERVICE_INSTANCE_PACKAGE);
        interfaceImplementationMap = IntStream.range(0, interfacesList.size())
                .boxed()
                .collect(Collectors.toMap(interfacesList::get, implementsList::get));
        checkInstancesMap(interfaceImplementationMap);
    }

    public static Injector getInjector() {
        if (injector == null) {
            injector = new Injector();
        }
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> clazz = findImplementation(interfaceClazz);
        Object clazzImplementationInstance = null;
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                clazzImplementationInstance = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, getInstance(field.getType()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can`t initialize field value. Clazz: "
                            + clazz.getName() + ". Field: " + field.getName());
                }
            }
        }
        return clazzImplementationInstance == null ? createNewInstance(clazz)
                : clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Can`t create instances of class: " + clazz.getName()
                    + ". No Component anotattion");
        }
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can`t create instance of: " + clazz, e);
        }
    }

    private Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInstance(interfaceClazz)) {
            return interfaceClazz;
        }
        return interfaceImplementationMap.get(interfaceClazz);
    }

    private void checkInstancesMap(Map<Class<?>, Class<?>> map) {
        map.forEach((key, value) -> {
            if (key.isInstance(value)) {
                throw new RuntimeException("Some problem with creating map of instances. "
                        + "Interface: " + key + " and instance: " + value);
            }
        });
    }
}
