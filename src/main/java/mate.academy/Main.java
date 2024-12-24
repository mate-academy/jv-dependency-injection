package mate.academy;

import java.util.List;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.ProductService;

public class Main {
    public static final String FILE_PATH = "src/main/resources/products.txt";

    public static void main(String[] args) {
        Injector injector = Injector.getInjector();
        ProductService productService =
                (ProductService) injector.getInstance(ProductService.class);
        List<Product> products = productService.getAllFromFile(FILE_PATH);
        products.forEach(System.out::println);
    }
}
