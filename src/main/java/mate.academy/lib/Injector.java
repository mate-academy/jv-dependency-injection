package mate.academy.lib;

import java.lang.reflect.Field;

public class Injector {
    private static final Injector injector = new Injector();

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Field[] fields = interfaceClazz.getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                //create a new object of field type
                //create an object of interface clazz, (or implementation class)
                // set field type object to interface clazz object
            }
        }
        return null;
    }
}
