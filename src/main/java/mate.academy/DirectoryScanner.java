package mate.academy;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import mate.academy.lib.Component;

public class DirectoryScanner {

    public Map<Class<?>, Class<?>> getComponents(String directoryName, String packageName) {
        File directory = new File(directoryName);
        String[] files = directory.list();
        Map<Class<?>, Class<?>> implementations = new HashMap<>();
        for (String file : files) {
            if (!file.endsWith(".java")) {
                continue;
            }
            String classFullName = packageName + '.'
                    + file.substring(0, file.length() - 5);
            Class<?> clazz = null;
            try {
                clazz = Class.forName(classFullName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Error during file defining. File=" + classFullName, e);
            }
            if (clazz.isAnnotationPresent(Component.class)) {
                Component annotation = clazz.getAnnotation(Component.class);
                Class<?> interfaceImplementedByClazz = annotation.implementationOf();
                implementations.put(interfaceImplementedByClazz, clazz);
            }
        }
        return implementations;
    }
}
