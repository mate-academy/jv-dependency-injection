package mate.academy.service.impl;

import mate.academy.lib.Component;
import mate.academy.lib.Inject;
import mate.academy.model.Product;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.FileReaderService;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductServiceImpl implements ProductService {
    @Inject
    private ProductParser productParser;
    @Inject
    private FileReaderService fileReaderService;

    @Override
    public List<Product> getAllProducts() {
        return fileReaderService.readFile("products.txt")
                .stream()
                .map(productParser::parse)
                .collect(Collectors.toList());
    }
}
