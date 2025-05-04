package mate.academy.lib;

import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.reflect.Field;
import java.util.Arrays;

class InjectorTest {
    private Injector injector = Injector.getInjector();

    @Test
    public void component_isRetentionSet() {
        boolean isPresent = Arrays.stream(Component.class.getAnnotations())
                .anyMatch(annotation -> annotation.annotationType().equals(Retention.class));
        Assertions.assertTrue(isPresent, "Component  "
                + "should have Retention annotation with policy RUNTIME");
    }

    @Test
    public void inject_isRetentionSet() {
        boolean isPresent = Arrays.stream(Inject.class.getAnnotations())
                .anyMatch(annotation -> annotation.annotationType().equals(Retention.class));
        Assertions.assertTrue(isPresent, "Inject  "
                + "should have Retention annotation with policy RUNTIME");
    }

    @Test
    public void fileReaderServiceImpl_isAnnotationPresent() {
        boolean isPresent = Arrays.stream(FileReaderServiceImpl.class.getAnnotations())
                .anyMatch(annotation -> annotation.annotationType().equals(Component.class));
        Assertions.assertTrue(isPresent, "FileReaderService implementation "
                + "should be marked with Component annotation");
    }

    @Test
    public void productServiceImpl_isAnnotationPresent() {
        boolean isPresent = Arrays.stream(ProductServiceImpl.class.getAnnotations())
                .anyMatch(annotation -> annotation.annotationType().equals(Component.class));
        Assertions.assertTrue(isPresent, "ProductService implementation "
                + "should be marked with Component annotation");
    }

    @Test
    public void productParserImpl_isAnnotationPresent() {
        boolean isPresent = Arrays.stream(ProductParserImpl.class.getAnnotations())
                .anyMatch(annotation -> annotation.annotationType().equals(Component.class));
        Assertions.assertTrue(isPresent, "ProductParser implementation "
                + "should be marked with Component annotation");
    }

    @Test
    public void getInstance_invalidClassThrowsException() {
        Assertions.assertThrows(RuntimeException.class, () -> injector.getInstance(String.class)
                , "When unsupported class is passed you should throw RuntimeException");
    }

    @Test
    public void getInstance_productParserInstance() {
        Object actual = injector.getInstance(ProductParser.class);

        Assertions.assertTrue(actual instanceof ProductParser,
                "Injector should be able to generate instance of ProductParser");
    }

    @Test
    public void getInstance_productServiceInstance() {
        Object actual = injector.getInstance(ProductService.class);

        Assertions.assertTrue(actual instanceof ProductService,
                "Injector should be able to generate instance of ProductService");
    }

    @Test
    public void getInstance_fileReaderInstance() {
        Object actual = injector.getInstance(FileReaderService.class);

        Assertions.assertTrue(actual instanceof FileReaderService,
                "Injector should be able to generate instance of FileReader");
    }

    @Test
    public void getInstance_nestedInstanceCreated() {
        Field[] serviceFields = ProductServiceImpl.class.getDeclaredFields();
        boolean areAllPresent = Arrays.stream(serviceFields)
                .allMatch(field -> {
                    return Arrays.stream(field.getDeclaredAnnotations())
                            .anyMatch(annotation -> annotation.annotationType().equals(Inject.class));
                });

        Assertions.assertTrue(areAllPresent, "ProductServiceImpl fields "
                + "should be marked with Inject annotation to be initialized");
    }
}