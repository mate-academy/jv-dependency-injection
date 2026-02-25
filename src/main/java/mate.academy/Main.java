package mate.academy;

import java.util.List;

import mate.academy.lib.Component;
import mate.academy.lib.Inject;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.ProductService;

public class Main {

    public static void main(String[] args) {
        // Please test your Injector here. Feel free to push this class as a part of your solution
        Injector injector = Injector.getInjector();
        injector.getInstance(ProductService.class);
        Object object = injector.getInstance(ProductService.class);
        ProductService productService = (ProductService) object;
        List<Product> products = productService.getAllFromFile("products.txt");
        products.forEach(System.out::println);
    }
}
