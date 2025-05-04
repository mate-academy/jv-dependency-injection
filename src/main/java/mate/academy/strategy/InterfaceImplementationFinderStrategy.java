package mate.academy.strategy;

import java.util.Set;
import mate.academy.lib.Component;
import org.reflections.Reflections;

public interface InterfaceImplementationFinderStrategy {
    String PACKAGE_NAME = "mate.academy";

    static Class<?> getImplementation(Class<?> interfaceClass) {
        Reflections reflections = new Reflections(PACKAGE_NAME);
        Set<?> classes = reflections.getSubTypesOf(interfaceClass);
        return (Class<?>) classes.stream()
                .filter(clazz -> ((Class<?>) clazz).isAnnotationPresent(Component.class))
                .findFirst().orElseThrow(() -> new RuntimeException(
                        "Interface " + interfaceClass.getName()
                                + " doesn't have any implementation"
                                + " marked with annotation '@Component'"
                ));
    }
}
