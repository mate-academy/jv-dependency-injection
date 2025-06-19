package mate.academy;

import java.util.List;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.ProductService;

public class Main {
    public static void main(String[] args) {
        ProductService productService = (ProductService)
                Injector.getInstance("mate.academy.service.ProductServiceImpl");
        List<Product> products = productService.getAllFromFile("products.txt");
        products.forEach(System.out::println);
    }
}

