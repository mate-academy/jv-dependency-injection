package mate.academy.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import mate.academy.lib.Component;
import mate.academy.lib.Inject;
import mate.academy.model.Product;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;

@Component
public class ProductServiceImpl implements ProductService {
    @Inject
    private ProductParser productParserService;
    @Inject
    private FileReaderService productFileReaderService;

    @Override
    public List<Product> getAllFromFile(String filePath) {
        return productFileReaderService.readFile(filePath)
                .stream()
                .map(productParserService::parse)
                .collect(Collectors.toList());
    }
}
