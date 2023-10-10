package mate.academy;

import java.util.List;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.ProductService;

public class Main {

    private static final String FILE_PRODUCTS_TXT = "products.txt";

    public static void main(String[] args) {
        // Please test your Injector here. Feel free to push this class as a part of your solution
        Injector injector = Injector.getInjector();
        ProductService productService =
                (ProductService) injector.getInstance(ProductService.class);
        List<Product> products = productService.getAllFromFile(FILE_PRODUCTS_TXT);
        products.forEach(System.out::println);
    }
}
