package mate.academy.service.impl;

import java.math.BigDecimal;
import mate.academy.lib.Component;
import mate.academy.model.Product;
import mate.academy.service.ProductParser;

@Component
public class ProductParserImpl implements ProductParser {
    public static final int ID_POSITION = 0;
    public static final int NAME_POSITION = 1;
    public static final int CATEGORY_POSITION = 2;
    public static final int DESCRIPTION_POSITION = 3;
    public static final int PRICE_POSITION = 4;

    @Override
    public Product parse(String productInfo) {
        String[] data = productInfo.split(",");
        Product product = new Product();
        product.setId(Long.valueOf(data[ID_POSITION]));
        product.setName(data[NAME_POSITION]);
        product.setCategory(data[CATEGORY_POSITION]);
        product.setDescription(data[DESCRIPTION_POSITION]);
        product.setPrice(new BigDecimal(data[PRICE_POSITION]));
        return product;
    }
}
