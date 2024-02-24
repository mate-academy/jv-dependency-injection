package mate.academy;

import java.util.List;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductService;

public class Main {

    public static void main(String[] args) {
        Injector injector = Injector.getInjector();

        ProductService productService = (ProductService) injector.getInstance(ProductService.class);
        FileReaderService fileReaderService =
                (FileReaderService) injector.getInstance(FileReaderService.class);

        List<String> stringsProducts = fileReaderService.readFile("products.txt");
        stringsProducts.forEach(System.out::println);
        System.out.println();
        List<Product> products = productService.getAllFromFile("products.txt");
        products.forEach(System.out::println);
    }
}
