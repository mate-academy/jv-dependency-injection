package mate.academy;

import java.util.List;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.ProductService;

public class Main {
    private static final String FILE_NAME = "products.txt";

    public static void main(String[] args) {
        Injector injector = Injector.getInjector();
        ProductService productService = (ProductService) injector.getInstance(ProductService.class);

        List<Product> products = productService.getAllFromFile(FILE_NAME);
        products.forEach(System.out::println);
    }
}
