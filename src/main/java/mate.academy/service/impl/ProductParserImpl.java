package mate.academy.service.impl;

import java.math.BigDecimal;
import mate.academy.lib.Component;
import mate.academy.model.Product;
import mate.academy.service.ProductParser;

@Component
public class ProductParserImpl implements ProductParser {
    @Override
    public Product parse(String productInfo) {
        String[] parts = productInfo.split(",");
        if (parts.length != 5) {
            throw new RuntimeException("Invalid product format: " + productInfo);
        }

        Product product = new Product();
        product.setId(Long.parseLong(parts[0].trim()));
        product.setName(parts[1].trim());
        product.setCategory(parts[2].trim());
        product.setDescription(parts[3].trim());
        product.setPrice(new BigDecimal(parts[4].trim()));
        return product;
    }
}
