package mate.academy;

import java.util.List;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.ProductService;

public class Main {

    public static void main(String[] args) throws IllegalAccessException {

        Injector injector = Injector.getInjector();

        injector.scanPackage("mate.academy.service.impl");

        ProductService productService =
                (ProductService) injector.getInstance(ProductService.class);
        List<Product> products = productService.getAllFromFile("products.txt");
        products.forEach(System.out::println);
    }
}
