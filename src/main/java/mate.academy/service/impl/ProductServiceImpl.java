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
    private FileReaderService fileReaderService;
    @Inject
    private ProductParser productParser;

    @Override
    public List<Product> getAllFromFile(String filePath) {
        if (fileReaderService == null || productParser == null) {
            throw new RuntimeException("Can`t get all product because this "
                    + productParser + " or " + fileReaderService
                    + " is null");
        }
        return fileReaderService.readFile(filePath)
                .stream()
                .map(productParser::parse)
                .collect(Collectors.toList());
    }
}
