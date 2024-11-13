package mate.academy.lib;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Injector {
    public static final String PATH_DELIMITER = "/";
    public static final String CLASSNAME_DELIMITER = ".";
    private static final Injector injector = new Injector();
    private static final String SERVICE_IMPL_PACKAGE = "mate/academy/service/impl";
    private static final String SERVICE_PACKAGE = "mate/academy/service";
    private static final String CLASS_EXTENSION = ".class";
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Class<?>> implementations;

    private Injector() {
        List<Class<?>> interfaces = findInterfaces();
        implementations = findImplementations(interfaces);
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Class<?> implementationClazz = implementations.get(interfaceClazz);
        boolean isPresent = instances.containsKey(implementationClazz);
        if (isPresent) {
            return instances.get(implementationClazz);
        }
        if (implementationClazz != null) {
            try {
                Object o = implementationClazz.getDeclaredConstructor().newInstance();
                Field[] fields = o.getClass().getDeclaredFields();
                for (Field field : fields) {
                    Annotation[] annotations = field.getAnnotations();
                    if (isAnnotationPresent(annotations, Inject.class)) {
                        field.setAccessible(true);
                        field.set(o, getInstance(field.getType()));
                        field.setAccessible(false);
                    }
                }
                instances.put(implementationClazz, o);
                return o;

            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(
                        String.format("Failed to create instance of %s", interfaceClazz));
            }
        }
        throw new RuntimeException(
                "There is missing implementation with @Component annotation for class"
                        + interfaceClazz.getName());
    }

    private boolean isAnnotationPresent(Annotation[] annotations,
            Class<? extends Annotation> annotationClass) {
        return Arrays.stream(annotations)
                .anyMatch(annotation -> annotation.annotationType().equals(annotationClass));
    }

    private List<Class<?>> findClassesInPackage(String packageName) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try(InputStream stream = classLoader.getResourceAsStream(packageName)){
            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
                return br.lines()
                        .filter(i -> i.endsWith(CLASS_EXTENSION))
                        .map(i -> i.substring(0, i.indexOf(CLASS_EXTENSION)))
                        .map(i -> getClazz(packageName, i)).collect(Collectors.toList());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read classes in package:" + packageName);
        }
    }

    private Class<?> getClazz(String packageName, String className) {
        try {
            return Class.forName(
                    packageName.replace(PATH_DELIMITER, CLASSNAME_DELIMITER) + "." + className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    String.format("There is no class: %s in your project.", className));
        }
    }

    private List<Class<?>> findInterfaces() {
        List<Class<?>> interfaces = findClassesInPackage(SERVICE_PACKAGE);
        return interfaces.stream().filter(Class::isInterface).collect(Collectors.toList());
    }

    private Map<Class<?>, Class<?>> findImplementations(List<Class<?>> interfaces) {
        Map<Class<?>, Class<?>> impl = new HashMap<>();
        List<Class<?>> classes = findClassesInPackage(SERVICE_IMPL_PACKAGE);
        classes.stream().filter(this::isComponent).forEach(i -> {
            Class<?>[] implementedInterfaces = i.getInterfaces();
            for (Class<?> implementedInterface : implementedInterfaces) {
                if (interfaces.contains(implementedInterface)) {
                    impl.put(implementedInterface, i);
                }
            }
        });
        return impl;
    }

    private boolean isComponent(Class<?> i) {
        Annotation[] annotations = i.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(Component.class)) {
                return true;
            }
        }
        return false;
    }
}
