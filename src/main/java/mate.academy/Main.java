package mate.academy;

import java.util.List;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Main {

    public static void main(String[] args) {
        Injector injector = Injector.getInjector();

        ProductService productService = (ProductServiceImpl)
                injector.getInstance(ProductServiceImpl.class);
        List<Product> products = productService.getAllFromFile("products.txt");
        products.forEach(System.out::println);

        ProductParser productParser = (ProductParserImpl)
                injector.getInstance(ProductParserImpl.class);
        if (productParser == null) {
            throw new RuntimeException("Incorrect implementation for ProductParser");
        }

        FileReaderService fileReaderService = (FileReaderServiceImpl)
                injector.getInstance(FileReaderServiceImpl.class);
        if (fileReaderService == null) {
            throw new
                    RuntimeException("Incorrect implementation for FileReaderService");
        }
    }
}
