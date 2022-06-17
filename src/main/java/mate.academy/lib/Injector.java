package mate.academy.lib;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Injector {
    private static final String SERVICE_INTERFACE_PACKAGE_PATH =
            "target/classes/mate/academy/service";
    private static final String SERVICE_INSTANCE_PACKAGE_PATH =
            "target/classes/mate/academy/service/impl";
    private static final Injector injector = new Injector();
    private static final Map<Class<?>, Object> instances = new HashMap<>();
    private static Map<Class<?>, Class<?>> interfaceImplementationMap;

    {
        List<Class<?>> interfacesList = getListOfClass(Path.of(SERVICE_INTERFACE_PACKAGE_PATH));
        List<Class<?>> implementsList = getListOfClass(Path.of(SERVICE_INSTANCE_PACKAGE_PATH));
        interfaceImplementationMap = IntStream.range(0, interfacesList.size())
                .boxed()
                .collect(Collectors.toMap(interfacesList::get, implementsList::get));
        checkInstancesMap(interfaceImplementationMap);
    }

    public static Injector getInjector() {
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

    private List<Class<?>> getListOfClass(Path path) {
        List<Path> pathList;
        try {
            pathList = Files.list(path).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Can`t open path of package: " + path, e);
        }
        return pathList.stream().map(Path::toString)
                .filter(s -> s.contains(".class"))
                .map(s -> s.replace("target\\classes\\", "")
                        .replace(".class", "").replace("\\","."))
                .map(s -> {
                    try {
                        return Class.forName(s);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("Can`t create class type with name: " + s, e);
                    }
                })
                .collect(Collectors.toList());
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
