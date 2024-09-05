package mate.academy.service;

import mate.academy.model.Product;

public interface ProductParser {
    /**
     * Parses a product from a string.
     *
     * @param productInfo the string containing product information
     * @return newly create {@link Product} object based on the `productInfo` input.
     */
    Product parse(String productInfo);
}
