package mate.academy;

import mate.academy.lib.Injector;
import mate.academy.service.ProductService;

public class Main {

    public static void main(String[] args) {
        ProductService productService = (ProductService) Injector.getInjector()
                .getInstance(ProductService.class);
        productService.getAllFromFile("products.txt").forEach(System.out::println);
    }
}
