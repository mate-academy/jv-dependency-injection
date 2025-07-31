package mate.academy;

import java.util.List;
import mate.academy.lib.Component;
import mate.academy.lib.Inject;
import mate.academy.model.Product;
import mate.academy.service.ProductService;

/*
  add this class only for check, that my injector works with classes too
 */
@Component
public class Controller {
    @Inject
    private ProductService productService;

    public void execute() {
        List<Product> products = productService.getAllFromFile("products.txt");
        products.forEach(System.out::println);
    }
}
