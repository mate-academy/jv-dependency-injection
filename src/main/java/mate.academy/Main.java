package mate.academy;

import mate.academy.lib.Injector;
import mate.academy.service.ProductService;

public class Main {

    public static void main(String[] args) {

        Injector injector = Injector.getInjector();
        ProductService productService = (ProductService) injector.getInstance(ProductService.class);
        productService
                .getAllFromFile("products.txt")
                .forEach(System.out::println);
    }
}
