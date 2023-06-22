package mate.academy;

import java.util.List;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.ProductParserImpl;

public class Main {

    public static void main(String[] args) {
        // Please test your Injector here. Feel free to push this class as a part of your solution
        Injector injector = Injector.getInjector();
        ProductService productService = (ProductService) injector.getInstance(ProductService.class);
        FileReaderService fileReaderService = (FileReaderService) injector.getInstance(FileReaderService.class);
        ProductParser productParser = (ProductParser) injector.getInstance(ProductParser.class);
        List<Product> products = productService.getAllFromFile("products.txt");
        products.forEach(System.out::println);
    }
}
