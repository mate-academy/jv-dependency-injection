package mate.academy.service.impl;

import java.math.BigDecimal;
import mate.academy.lib.Component;
import mate.academy.model.Product;
import mate.academy.service.ProductParser;

@Component
public class ProductParserImpl implements ProductParser {
    private static final int NAME_POSITION = 0;
    private static final int PRICE_POSITION = 1;
    private static final int DESCRIPTION_POSITION = 2;

    @Override
    public Product parse(String productInfo) {
        String[] data = productInfo.split(",");
        Product product = new Product();
        product.setName(data[NAME_POSITION]);
        product.setPrice(new BigDecimal(data[PRICE_POSITION]));
        product.setDescription(data[DESCRIPTION_POSITION]);
        return product;
    }
}
