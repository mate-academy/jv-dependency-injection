package mate.academy.service.impl;

import mate.academy.lib.Component;
import mate.academy.model.Product;
import mate.academy.service.ProductParser;
import java.math.BigDecimal;
import java.util.List;

@Component
public class ProductParserImpl implements ProductParser {
    @Override
    public Product parse(String line) {
        String[] parts = line.split(",");
        String name = parts[0];
        BigDecimal price = new BigDecimal(parts[1]);
        return new Product(name, price);
    }
}
