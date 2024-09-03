package mate.academy.service.impl;

import mate.academy.lib.Component;
import mate.academy.lib.Inject;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.FileReaderService;

@Component
public class ProductServiceImpl implements ProductService {
    @Inject
    private ProductParser productParser;
    @Inject
    private FileReaderService fileReaderService;

    @Override
    public List<Product> getAllFromFile(String filePath) {
        return fileReaderService.readFile(filePath)
                .stream()
                .map(productParser::parse)
                .collect(Collectors.toList());
    }
}
