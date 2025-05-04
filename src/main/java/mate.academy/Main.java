package mate.academy;

import java.util.List;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.ProductService;
import mate.academy.service.impl.ProductServiceImpl;

public class Main {

    public static void main(String[] args) {

        Injector injector = Injector.getInjector();
        ProductService productService = (ProductServiceImpl) injector
                .getInstance(ProductServiceImpl.class);
        List<Product> products = productService.getAllFromFile("products.txt");
        products.forEach(System.out::println);
    }
}
