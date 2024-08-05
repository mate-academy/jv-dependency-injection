package mate.academy.service.impl;

import mate.academy.lib.Component;
import mate.academy.model.Product;
import mate.academy.service.ProductParser;

@Component
public class ProductParserImpl implements ProductParser {
    @Override
    public Product parse(String productInfo) {
        String[] parts = productInfo.split(",");
        String name = parts[0];
        double price = Double.parseDouble(parts[1]);
        return new Product(name, price);
    }
}
