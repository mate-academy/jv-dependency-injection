package mate.academy.service.impl;

import java.math.BigDecimal;
import mate.academy.lib.Component;
import mate.academy.model.Product;
import mate.academy.service.ProductParser;

@Component
public class ProductParserImpl implements ProductParser {
    public static final int POSITION_ID = 0;
    public static final int POSITION_NAME = 1;
    public static final int CATEGORY_POSITION = 2;
    public static final int POSITION_DESCRIPTION = 3;
    public static final int POSITION_PRICE = 4;

    @Override
    public Product parse(String productInfo) {
        String[] data = productInfo.split(",");
        Product product = new Product();
        product.setId(Long.valueOf(data[POSITION_ID]));
        product.setName(data[POSITION_NAME]);
        product.setCategory(data[CATEGORY_POSITION]);
        product.setDescription(data[POSITION_DESCRIPTION]);
        product.setPrice(new BigDecimal(data[POSITION_PRICE]));
        return product;
    }
}
