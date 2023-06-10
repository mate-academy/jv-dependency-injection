package mate.academy;

import java.util.List;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;

public class Main {

    public static void main(String[] args) {
        // Create an instance of the Injector
        Injector injector = Injector.getInjector();

        FileReaderService fileReaderService = injector.getInstance(FileReaderService.class);
        ProductParser productParser = injector.getInstance(ProductParser.class);
        final ProductService productService = injector.getInstance(ProductService.class);

        // Test the FileReaderService functionality
        List<String> lines = fileReaderService.readFile("example.txt");
        System.out.println("File contents:");
        lines.forEach(System.out::println);

        // Test the ProductParser functionality
        String productInfo = "1,Product A,Category A,Description A,10.99";
        Product product = productParser.parse(productInfo);
        System.out.println("Parsed product:");
        System.out.println(product);

        // Test the ProductService functionality
        List<Product> products = productService.getAllFromFile("products.txt");
        System.out.println("All products:");
        products.forEach(System.out::println);
    }
}
