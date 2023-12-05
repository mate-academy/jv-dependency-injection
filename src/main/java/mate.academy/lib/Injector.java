package mate.academy.lib;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Injector {
    private static final String PACKAGE_IMPL = "mate.academy.service.impl";
    private static final String PACKAGE_INTERFACE = "mate.academy.service";
    private static final String CLASS_EXTENSION = ".class";
    private static final String DOT = ".";
    private static final String DOT_REGEX = "[.]";
    private static final String SLASH = "/";
    private static final int STRING_BEGINNING_INDEX = 0;
    private static final Injector injector = new Injector();
    private static final Map<Class<?>, Class<?>> implementationsMap = new HashMap<>();

    public static Injector getInjector() {
        initialiseInjector();
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object object = createObject(implementationsMap
                .get(interfaceClazz));
        Field[] declaredFields = implementationsMap
                .get(interfaceClazz)
                .getDeclaredFields();
        injectAnnotatedFields(object, declaredFields);
        return object;
    }

    private void injectAnnotatedFields(Object object,
                                       Field[] declaredFields) {
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                Class<?> type = field.getType();
                Object innerObject = createObject(implementationsMap.get(type));
                try {
                    field.set(object, innerObject);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Something went wrong "
                            + "with creating an object of class "
                            + implementationsMap.get(type).getName(), e);
                }
            }
        }
    }

    private static void initialiseInjector() {
        Set<Class<?>> interfaces = findClasses(PACKAGE_INTERFACE);
        Set<Class<?>> classes = findClasses(PACKAGE_IMPL);
        for (Class<?> currentInterface : interfaces) {
            if (!implementationsMap.containsKey(currentInterface)) {
                Class<?> currentClass = classes.stream()
                        .filter(i -> i.getName()
                                .contains(currentInterface
                                        .getName()
                                        .substring(currentInterface
                                                .getName()
                                                .lastIndexOf(DOT))))
                        .findFirst()
                        .orElseThrow();
                if (currentClass.isAnnotationPresent(Component.class)) {
                    implementationsMap.put(currentInterface, currentClass);
                }
            }
        }
    }

    private static Set<Class<?>> findClasses(String packageValue) {
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageValue.replaceAll(DOT_REGEX, SLASH));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(CLASS_EXTENSION))
                .map(line -> getClass(line, packageValue))
                .collect(Collectors.toSet());
    }

    private static Class<?> getClass(String className, String packageValue) {
        try {
            return Class.forName(packageValue + DOT
                    + className.substring(STRING_BEGINNING_INDEX,
                    className.lastIndexOf(DOT)));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not fount "
                    + className, e);
        }
    }

    private Object createObject(Class<?> objectClass) {
        Object object;
        try {
            object = objectClass.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Something went wrong "
                    + "with creating an object of class "
                    + objectClass.getName(), e);
        }
        return object;
    }
}
