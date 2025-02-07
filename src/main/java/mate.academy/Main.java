package mate.academy;

import java.util.List;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.ProductServiceImpl;

public class Main {

    public static void main(String[] args) {
        Injector injector = Injector.getInjector();
        ProductService productService
                = (ProductService) injector.getInstance(ProductServiceImpl.class);
        List<Product> products = productService.getAllFromFile("products.txt");
        products.forEach(System.out::println);

        ProductService productService2
                = (ProductService) injector.getInstance(ProductService.class);
        List<Product> products2 = productService2.getAllFromFile("products.txt");
        products2.forEach(System.out::println);

        ProductParser productParser = (ProductParser) injector.getInstance(ProductParser.class);
        productParser.parse("12,Iphone 18,phones,amazing,1999");

        FileReaderService fileReaderService
                = (FileReaderService) injector.getInstance(FileReaderService.class);
        System.out.println(fileReaderService.readFile("products.txt"));

    }
}
