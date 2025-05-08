package mate.academy;

import java.util.List;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductService;


public class Main {

    public static void main(String[] args) {
        Injector injector = Injector.getInjector();
        Object actual = injector.getInstance(FileReaderService.class);
        System.out.println(actual);

//        Injector injector = Injector.getInjector();
//        ProductService productService = null;
//        List<Product> products = productService.getAllFromFile("products.txt");
//        products.forEach(System.out::println);
    }
}
