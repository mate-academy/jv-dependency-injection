package mate.academy;

import java.util.List;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Main {

    public static void main(String[] args) {
        Injector injector = Injector.getInjector();
        ProductService productServiceImpl
                = (ProductService) injector.getInstance(ProductServiceImpl.class);
        List<Product> products = productServiceImpl.getAllFromFile("products.txt");
        products.forEach(System.out::println);

        ProductService productService
                = (ProductService) injector.getInstance(ProductService.class);
        List<Product> products2 = productService.getAllFromFile("products.txt");
        products2.forEach(System.out::println);

        ProductParser productParser = (ProductParser) injector.getInstance(ProductParser.class);
        productParser.parse("12,Iphone 18,phones,amazing,1999");

        ProductParser productParserImpl
                = (ProductParser) injector.getInstance(ProductParserImpl.class);
        productParserImpl.parse("33,Iphone 28,phones,Too cheap,4999");

        FileReaderService fileReaderService
                = (FileReaderService) injector.getInstance(FileReaderService.class);
        System.out.println(fileReaderService.readFile("products.txt"));

        FileReaderService fileReaderServiceImpl
                = (FileReaderService) injector.getInstance(FileReaderService.class);
        System.out.println(fileReaderServiceImpl.readFile("products.txt"));

        String incorrectObject = (String) injector.getInstance(String.class);
    }
}
