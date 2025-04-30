package mate.academy.service.impl;

import mate.academy.lib.Component;
import mate.academy.lib.Inject;
import mate.academy.model.Product;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductServiceImpl implements ProductService {
    @Inject
    private FileReaderService fileReaderService;

    @Inject
    private ProductParser productParser;

    @Override
    public List<Product> getAllFromFile(String filePath) {
        return fileReaderService.readFile(filePath).stream()
                .map(productParser::parse)
                .collect(Collectors.toList());
    }
}
