package mate.academy.service.impl;

import mate.academy.lib.Component;
import mate.academy.lib.Inject;
import mate.academy.service.FileReaderService;
import mate.academy.model.Product;
import java.util.List;
import java.util.stream.Collectors;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;

@Component // Ensure this annotation is present
public class ProductServiceImpl implements ProductService {

    private final ProductParser productParser;
    private final FileReaderService fileReaderService;

    @Inject
    public ProductServiceImpl(ProductParser productParser, FileReaderService fileReaderService) {
        this.productParser = productParser;
        this.fileReaderService = fileReaderService;
    }

    @Override
    public List<Product> getAllFromFile(String filePath) {
        return fileReaderService.readFile(filePath)
                .stream()
                .map(productParser::parse)
                .collect(Collectors.toList());
    }
}
