package mate.academy.service.impl;

import java.math.BigDecimal;
import mate.academy.lib.Component;
import mate.academy.model.Product;
import mate.academy.service.ProductParser;

@Component
public class ProductParserImpl implements ProductParser {

    @Override
    public Product parse(String productInfo) {
        String[] data = productInfo.split(",");

        Product product = new Product();
        product.setId(Long.parseLong(data[0]));
        product.setName(data[1]);
        product.setCategory(data[2]);
        product.setDescription(data[3]);
        product.setPrice(new BigDecimal(data[4]));

        return product;
    }
}

