package mate.academy;

import java.util.List;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.ProductService;

public class Main {
    private static final String INPUT_FILE = "products.txt";

    public static void main(String[] args) {
        Injector injector = Injector.getInjector();
        ProductService productService = (ProductService) injector
                .getInstance(ProductService.class);
        List<Product> products = productService.getAllFromFile(INPUT_FILE);
        products.forEach(System.out::println);
    }
}
