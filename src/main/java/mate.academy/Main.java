package mate.academy;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.ProductService;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        Injector injector = Injector.getInjector();
        try {
            ProductService productService = (ProductService) injector
                    .getInstance(ProductService.class);
            List<Product> products = productService.getAllFromFile("products.txt");
            products.forEach(System.out::println);
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Runtime exception occurred: " + e.getMessage(), e);
        }
    }
}
