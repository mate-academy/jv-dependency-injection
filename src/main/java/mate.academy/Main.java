package mate.academy;

import java.util.List;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.ProductService;
import mate.academy.service.impl.ProductServiceImpl;

public class Main {
    private static final Injector injector = new Injector();

    public static void main(String[] args) {
        ProductService productService =
                (ProductServiceImpl) injector.getInstance(ProductServiceImpl.class);
        List<Product> products = productService.getAllFromFile("products.txt");
        products.forEach(System.out::println);
    }
}
