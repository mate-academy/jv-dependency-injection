package mate.academy.lib;

import java.lang.annotation.Annotation;

public class Injector implements Inject{
    private static final Injector injector = new Injector();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        return null;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }
}
