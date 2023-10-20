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
public class ProductServiceImpl extends Injector implements ProductService {
    @Inject
    private final FileReaderService fileReaderService;
    @Inject
    private final ProductParser productParser;

    public ProductServiceImpl() {
        this.fileReaderService = new FileReaderServiceImpl();
        this.productParser = new ProductParserImpl();
    }

    @Override
    public Object getInstance(Class<?> interfaceClazz) {
        return super.getInstance(interfaceClazz);
    }

    @Override
    public List<Product> getAllFromFile(String filePath) {
        return fileReaderService.readFile(filePath)
                .stream()
                .map(productParser::parse)
                .collect(Collectors.toList());
    }
}
