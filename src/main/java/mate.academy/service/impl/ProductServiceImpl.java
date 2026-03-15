package mate.academy.service.impl;

import java.util.ArrayList;
import java.util.List;
import mate.academy.lib.Component;
import mate.academy.lib.Inject;
import mate.academy.model.Product;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;

@Component
public class ProductServiceImpl implements ProductService {

    @Inject
    private ProductParser productParser;

    @Inject
    private FileReaderService fileReaderService;

    @Override
    public List<Product> getAllFromFile(String filePath) {
        List<String> lines = fileReaderService.readFile(filePath);
        List<Product> products = new ArrayList<>();

        for (String line : lines) {
            products.add(productParser.parse(line));
        }

        return products;
    }
}

