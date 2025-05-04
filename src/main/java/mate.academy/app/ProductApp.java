package mate.academy.app;

import java.util.List;
import mate.academy.lib.Component;
import mate.academy.lib.Inject;
import mate.academy.model.Product;
import mate.academy.service.ProductService;

@Component
public class ProductApp {
    private static final String FILE_PATH = "products.txt";
    @Inject
    private ProductService productService;

    public void createListOfProducts() {
        List<Product> products = productService.getAllFromFile(FILE_PATH);
        products.forEach(System.out::println);
    }
}
