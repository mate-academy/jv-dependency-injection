package mate.academy.lib;

import java.lang.annotation.Annotation;

public class Injector {
    private static final Injector injector = new Injector();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClass) {
        Class<?> implementationClass = ImplementationMap.get(interfaceClass);
        if (!isComponent(implementationClass)) {
            throw new RuntimeException("Injection failed, missing @Component annotaion on the class "
                    + implementationClass.getName());
        }
        return null;
    }

    private boolean isComponent(Class<?> implementationClass) {
        Annotation[] classAnnotations = implementationClass.getAnnotations();
        for (Annotation annotation : classAnnotations) {
            if (annotation instanceof Component) {
                return true;
            }
        }
        return false;
    }

}
