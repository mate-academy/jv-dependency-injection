package mate.academy;

import mate.academy.lib.Injector;
import mate.academy.service.ProductService;

public class Main {
    private static final String FILE_PATH = "products.txt";

    public static void main(String[] args) {
        Injector injector = Injector.getInjector();
        ProductService productService = (ProductService) injector.getInstance(ProductService.class);
        productService.getAllFromFile(FILE_PATH).forEach(System.out::println);
    }
}
