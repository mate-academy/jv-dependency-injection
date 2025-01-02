package mate.academy;

import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.ProductService;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        Injector injector = Injector.getInjector();
        ProductService productService = injector.getInstance(ProductService.class); // Retrieve ProductService instance from Injector
        List<Product> products = productService.getAllFromFile("products.txt");
        products.forEach(System.out::println);
    }
}
