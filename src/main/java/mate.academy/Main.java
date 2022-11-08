package mate.academy;

import java.util.List;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Main {

    public static void main(String[] args) {
        // Please test your Injector here. Feel free to push this class as a part of your solution
        Injector injector = Injector.getInjector();
        ProductServiceImpl productService = (ProductServiceImpl) injector
                .getInstance(ProductParserImpl.class);
        List<Product> products = productService
                .getAllFromFile("products.txt");
        products.forEach(System.out::println);
    }
}
