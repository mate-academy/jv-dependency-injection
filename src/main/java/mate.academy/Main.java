package mate.academy;

import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.ProductService;

public class Main {

    public static void main(String[] args) {
        Injector injector = Injector.getInjector();
        ProductService productService = (ProductService) injector.getInstance(ProductService.class);
        productService.getAllFromFile("src/main/resources/products.txt");
        for (Product p : productService.getAllFromFile("src/main/resources/products.txt")) {
            System.out.println(p.toString());
        }
    }
}
