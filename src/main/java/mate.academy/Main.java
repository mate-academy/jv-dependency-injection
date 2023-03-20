package mate.academy;

import java.lang.reflect.Field;
import java.util.List;
import mate.academy.lib.Injector;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductService;

public class Main {

    public static void main(String[] args) {
        Injector injector = Injector.getInjector();

        FileReaderService readerService = (FileReaderService) injector.getInstance(FileReaderService.class);
        System.out.println(readerService.toString());

        ProductService productService = (ProductService) injector.getInstance(ProductService.class);
        System.out.println(productService.toString());
        Field [] fields = productService.getClass().getDeclaredFields();
        for(Field field : fields) {
            System.out.println(field.getName());
        }

        Object actual = injector.getInstance(List.class);
        System.out.println(actual.toString());
    }
}
