package mate.academy;

import mate.academy.lib.Injector;
import mate.academy.service.ProductService;

public class Main {

    public static void main(String[] args) {
        Injector injector = Injector.getInjector();
        ProductService productService = (ProductService) injector.getInstance(ProductService.class);
        String filePath = "products.txt";
        productService.getAllFromFile(filePath).forEach(System.out::println);
    }
}
