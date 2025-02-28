package mate.academy.service.impl;

import mate.academy.lib.Component;
import mate.academy.model.Product;
import mate.academy.service.ProductParser;

import java.math.BigDecimal;

@Component
public class ProductParserImpl implements ProductParser {
    @Override
    public Product parse(String productInfo) {
        String[] parts = productInfo.split(",");
        Product product = new Product();
        product.setId(Long.parseLong(parts[0]));
        product.setName(parts[1]);
        product.setCategory(parts[2]);
        product.setDescription(parts[3]);
        product.setPrice(new BigDecimal(parts[4]));
        return product;
    }
}
