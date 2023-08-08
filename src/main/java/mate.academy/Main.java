package mate.academy;

import java.util.List;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.ProductService;

public class Main {

    public static void main(String[] args) {
        Injector injector = Injector.getInjector();
        Object productServiceObj = injector.getInstance(ProductService.class);

        List<Product> products;
        if (productServiceObj instanceof ProductService) {
            ProductService productService = (ProductService) productServiceObj;
            products = productService.getAllFromFile("products.txt");
            products.forEach(System.out::println);
        } else {
            System.out.println("Couldn't cast to ProductService");
        }
    }
}
