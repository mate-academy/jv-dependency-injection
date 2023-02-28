package mate.academy.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import mate.academy.lib.Component;
import mate.academy.lib.Inject;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;

@Component
public class ProductServiceImpl implements ProductService {
    @Inject
    private final ProductParser productParser;
    @Inject
    private final FileReaderService fileReaderService;

    public ProductServiceImpl() {
        Injector injector = Injector.getInjector();
        productParser = (ProductParser) injector.getInstance(ProductParser.class);
        fileReaderService = (FileReaderService) injector.getInstance(FileReaderService.class);
    }

    @Override
    public List<Product> getAllFromFile(String filePath) {
        return fileReaderService.readFile(filePath)
            .stream()
            .map(productParser::parse)
            .collect(Collectors.toList());
    }
}
