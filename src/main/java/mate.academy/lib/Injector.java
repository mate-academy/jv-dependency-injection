package mate.academy.lib;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Injector {
    private static Injector injector = new Injector("mate.academy");
    private Map<Class<?>, Object> instances = new HashMap<>();
    private Map<Class<?>, Object> instanceOfClasses = new HashMap<>();
    private List<Class<?>> classes = new ArrayList<>();

    private Injector(String mainPackageName) {
        classes.addAll(getClasses(mainPackageName));
    }

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> certainInterface) {
        Object instanceOfClass = null;
        Class<?> clazz = findClassExtendingInterface(certainInterface);
        Object instanceOfCurrentClass = createNewInstance(clazz);
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (isFieldInitialized(field, instanceOfCurrentClass)) {
                continue;
            }
            if (field.getDeclaredAnnotation(Inject.class) != null) {
                Object classToInject = getInstance(field.getType());
                instanceOfClass = getNewInstance(clazz);
                setValueToField(field, instanceOfClass, classToInject);
            } else {
                throw new RuntimeException("Injection failed. Field " + field.getName()
                        + " in class " + clazz.getName() + " doesn't have @Inject annotation");
            }
        }
        if (instanceOfClass == null) {
            return getNewInstance(clazz);
        }
        return instanceOfClass;
    }

    private List<Class<?>> getClasses(String mainPackageName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            throw new RuntimeException("Can't get information about classes. "
                    + "Classloader is null");
        }
        String path = mainPackageName.replace('.', '/');
        Enumeration<URL> resources = null;
        try {
            resources = classLoader.getResources(path);
        } catch (IOException e) {
            throw new RuntimeException("Can't get information about classes. "
                        + "Can't return resources", e);
        }
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, mainPackageName));
        }
        return classes;
    }

    private List<Class<?>> findClasses(File directory, String packageName) {
        File[] files = directory.listFiles();
        if (files == null) {
            throw new RuntimeException("Can't get information about classes."
                    + " File is null");
        }
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "."
                        + file.getName()));
            }
            if (file.getName().endsWith(".class")) {
                try {
                    classes.add(Class.forName(packageName + "."
                            + file.getName().substring(0, file.getName().length() - 6)));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Can't get information about classes. "
                            + "Class not found", e);
                }
            }
        }
        return classes;
    }

    private void setValueToField(Field field, Object instanceOfClass, Object classToInject) {
        try {
            field.set(instanceOfClass, classToInject);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Injection failed. Can't set value to the field "
                    + field.getName(), e);
        }
    }

    private Object getNewInstance(Class<?> clazz) {
        if (instanceOfClasses.containsKey(clazz)) {
            return instanceOfClasses.get(clazz);
        }
        Object newInstance = createNewInstance(clazz);
        instanceOfClasses.put(clazz, newInstance);
        return newInstance;
    }

    private boolean isFieldInitialized(Field field, Object instanceOfClass) {
        try {
            field.setAccessible(true);
            return field.get(instanceOfClass) != null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Injection failed. Can't check the field "
                    + field.getName(), e);
        }
    }

    private Class<?> findClassExtendingInterface(Class<?> certainInterface) {
        if (!certainInterface.isInterface()
                && certainInterface.isAnnotationPresent(Component.class)) {
            return certainInterface;
        }
        for (Class<?> clazz : classes) {
            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> singleInterface : interfaces) {
                if (singleInterface.equals(certainInterface)
                        && clazz.isAnnotationPresent(Component.class)) {
                    return clazz;
                }
            }
        }
        throw new RuntimeException("Injection failed. @Component annotation is not present in "
        + certainInterface.getName());
    }

    private Object createNewInstance(Class<?> clazz) {
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }
        Constructor<?> constructor = null;
        try {
            constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Injection failed. Can't create a new instance of "
                    + clazz.getName(), e);
        }
    }
}
