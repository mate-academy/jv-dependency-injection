package mate.academy.lib;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Injector {
    private static final Injector injector = new Injector();
    private static final Map<Class<?>, Class<?>> interfacesImplementations;
    private static final Map<Class<?>, Object> instances = new HashMap<>();
    private static final String SERVICE_PACKAGE = "mate.academy.service";
    private static final String SERVICE_PACKAGE_IMPL = "mate.academy.service.impl";
    private static final String CLASS_INDICATOR = ".class";

    static {
        interfacesImplementations = interfacesImplementationsConfiguration();
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object clazzImplementationInstance = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                Object fieldInstance = getInstance(field.getType());
                clazzImplementationInstance = createNewInstance(clazz);
                field.setAccessible(true);
                try {
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can't initialize field value. Class: "
                            + clazz.getName() + ", field: " + field.getName());
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private static Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't create new instance of class - " + clazz.getName());
        }
    }

    private static Class<?> findImplementation(Class<?> interfaceClazz) {
        if (interfaceClazz.isInterface()) {
            if (!interfacesImplementations.containsKey(interfaceClazz)) {
                throw new RuntimeException("Can`t find interface " + interfaceClazz.getName());
            }
            Class<?> implementation = interfacesImplementations.get(interfaceClazz);
            if (implementation == null) {
                throw new RuntimeException("Interface " + interfaceClazz.getName()
                        + " has no implementations or they doesn't annotated by @Component");
            }
            return implementation;
        }
        componentCheck(interfaceClazz);
        return interfaceClazz;
    }

    private static Map<Class<?>, Class<?>> interfacesImplementationsConfiguration() {
        Map<Class<?>, Class<?>> interfacesImplementations = new HashMap<>();
        for (Class<?> clazzInterface : findServiceInterfaces()) {
            Class<?> interfaceImplementation = null;
            for (Class<?> clazzImplementation : findComponents()) {
                if (clazzInterface.isAssignableFrom(clazzImplementation)) {
                    interfaceImplementation = clazzImplementation;
                    break;
                }
            }
            System.out.println(clazzInterface + " " + interfaceImplementation);
            interfacesImplementations.put(clazzInterface, interfaceImplementation);
        }
        return interfacesImplementations;
    }

    private static List<Class<?>> findComponents() {
        return findClassesByPackage(SERVICE_PACKAGE_IMPL).stream()
                .filter(clazz -> clazz.isAnnotationPresent(Component.class))
                .toList();
    }

    private static List<Class<?>> findServiceInterfaces() {
        return findClassesByPackage(SERVICE_PACKAGE).stream()
                .filter(Class::isInterface)
                .toList();
    }

    public static List<Class<?>> findClassesByPackage(String packageName) {
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        File packageDirectory = new File(classLoader.getResource(path).getFile());
        File[] files = packageDirectory.listFiles();
        return Arrays.stream(files)
                .filter(File::isFile)
                .filter(file -> file.getName().endsWith(CLASS_INDICATOR))
                .map(file -> getClass(file.getName(), packageName))
                .collect(Collectors.toList());
    }

    private static Class<?> getClass(String className, String packageName) {
        try {
            String clearedClassName = className.substring(0,
                    className.length() - CLASS_INDICATOR.length());
            return Class.forName(packageName + "." + clearedClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't find class " + className);
        }
    }

    private static void componentCheck(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Component.class)) {
            throw new RuntimeException("Unsupported class " + clazz
                    + ", should be annotated by @Component");
        }

    }
}

