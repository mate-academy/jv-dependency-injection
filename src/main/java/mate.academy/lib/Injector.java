package mate.academy.lib;

import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Injector {
        private static final Injector injector = new Injector();

        public static Injector getInjector() {
            return injector;
        }

        Map<Class<?>, Object> instances = new HashMap<>();
        public Object getInstance(Class<?> interfaceClass) {
            Class<?> classs = findImplementation(interfaceClass);
            Object classImplementationInstance = null;
            Field[] declaredFields = classs.getDeclaredFields();
            if (classs.isAnnotationPresent(Component.class)) {
                for (Field field : declaredFields) {
                    if (field.isAnnotationPresent(Inject.class)) {
                        Object fieldInstance = getInstance(field.getType());
                        classImplementationInstance = createNewInstance(classs);
                        field.setAccessible(true);
                        try {
                            field.set(classImplementationInstance, fieldInstance);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Can't initialize field value Class "
                                    + classs.getName() + ". Field: "
                                    + field.getName(), e);
                        }
                    }
                }
            } else {
                throw new RuntimeException("Class" + classs + " couldn't have a instance, "
                        + "cause annotation @Component is not present.");
            }

            if (classImplementationInstance == null) {
                classImplementationInstance = createNewInstance(classs);
            }
            return classImplementationInstance;
        }

        private Object createNewInstance(Class<?> classs) {

            if (instances.containsKey(classs)) {
                return instances.get(classs);
            }
            Constructor<?> constructor = null;
            try {
                constructor = classs.getConstructor();
                Object object = constructor.newInstance();
                instances.put(classs, object);
                return object;
            } catch (NoSuchMethodException | InstantiationException
                     | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Can't create a new instance of" + classs.getName(), e);
            }
        }

        private Class<?> findImplementation(Class<?> interfaceClass) {
            Map<Class<?>, Class<?>> implementationMap = new HashMap<>();
            implementationMap.put(FileReaderService.class, FileReaderServiceImpl.class);
            implementationMap.put(ProductService.class, ProductServiceImpl.class);
            implementationMap.put(ProductParser.class, ProductParserImpl.class);
            if (interfaceClass.isInterface()) {
                return implementationMap.get(interfaceClass);
            }
            return interfaceClass;
        }
}
