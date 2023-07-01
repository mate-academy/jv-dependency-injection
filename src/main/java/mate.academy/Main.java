package mate.academy;

import java.util.List;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.ProductService;

public class Main {

    public static void main(String[] args) {
        // Please test your Injector here. Feel free to push this class as a part of your solution
        Injector injector = Injector.getInjector();
        ProductService service = (ProductService) injector.getInstance(ProductService.class);
        List<Product> products = service.getAllFromFile("products.txt");
        products.forEach(System.out::println);
    }
}
