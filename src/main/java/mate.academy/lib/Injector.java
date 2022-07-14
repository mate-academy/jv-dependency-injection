package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;
/* у нас есть главная точка входа, это Main нижектор решает главную проблему это зависимость, что бы
не прописывать каждый раз зависимость в ручную мы и создали класс инжектор,что бы программа понимала
что нужно инжектить мы создали аннотацию "Inject"
 */

public class Injector {
    private static final Injector injector = new Injector(); //создали новый объект инжектора
    private Map<Class<?>, Object> instances = new HashMap<>();// создали мапу с экземплярами

    public static Injector getInjector() { // мы создаем его в мейне и этот метод должен возращать
        // обхект этого инжектора, выше создаем один инстанс нашего инжектора
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) { //метод для получения экземпляра принимает
        // имя например ProductService а возращает его имплементацию что бы не нарушать SOLID
        Object clazzImplementationInstance = null; // создали переменную для класса имплементации типа объект
        Class<?> clazz = findImplemetation(interfaceClazz); // находим имплементацию интерфейса
        if (!clazz.isAnnotationPresent(Component.class)) { // если в пришедшем к нам классе нет
            // аннотации component тогда выбрасываем ошибку
            throw new RuntimeException("Injection failed, missing @Component annotation on "
                    + clazz.getName() + " class");
        }
        Field[] declaredFields = clazz.getDeclaredFields(); // метод getDeclaredFields выдаёт нам
        // перечень всех полей в классе и тут же мы их ложем в массив
        for (Field field : declaredFields) { // проходим по каждому полю
            if (field.isAnnotationPresent(Inject.class)) { // если у поля есть аннотация Inject
                Object fieldInstance = getInstance(field.getType()); //??? объяснение part1 9.30 спросить у игоря,
                // объяснение - рекурсивно вызываем наш же метод что бы избавитсья от зависимости
                clazzImplementationInstance = createNewInstance(clazz);// создаём новый метод createNewInstance
                // который будет создавать объект нашего класса, сюда мы должны передать
                // именно калсс имплементацию если вдруг у нас тут будет интерфейс
                try {
                    field.setAccessible(true); // не понял что делает сетАксесибл
                    field.set(clazzImplementationInstance, fieldInstance);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Can not initialize field value for Class: "
                    + clazz.getName() + ", Field: " + field.getName(), e);
                }
            }
        }
        if (clazzImplementationInstance == null) {
            clazzImplementationInstance = createNewInstance(clazz);
        }
        return clazzImplementationInstance;
    }

    private Object createNewInstance(Class<?> clazz) {// метод, который создаёт новый экземпляр
        if (instances.containsKey(clazz)) { // берём нашу мапу и проверяем есть ли в ней такой класс если есть вернёт тру
            return instances.get(clazz); // возвращаем нужный класс
        }
        try {
            Constructor<?> constructor = clazz.getConstructor();
            Object instance = constructor.newInstance();
            instances.put(clazz, instance);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can not create a new instance of " + clazz.getName(), e);
        }
    }

    private Class<?> findImplemetation(Class<?> interfaceClazz) {
        Map<Class<?>, Class<?>> interfaceImplementations = new HashMap<>(); // в качестве ключа у
        // нас класс интерфейс и в качестве значения класс который имплементирует этот интерфейс
        interfaceImplementations.put(FileReaderService.class, FileReaderServiceImpl.class);
        interfaceImplementations.put(ProductService.class, ProductServiceImpl.class);
        interfaceImplementations.put(ProductParser.class, ProductParserImpl.class);
        if (interfaceClazz.isInterface()) { // если наш класс интерфейс мы будем возвращать данные с нашей мапы
            return interfaceImplementations.get(interfaceClazz);
        }
        return interfaceClazz; // иначе мы будем возвращать наш interfaceClazz
    }
}
