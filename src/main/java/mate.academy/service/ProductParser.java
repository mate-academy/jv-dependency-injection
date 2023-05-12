package mate.academy.service;

import mate.academy.lib.Component;
import mate.academy.model.Product;

@Component
public interface ProductParser {
    Product parse(String productInfo);
}
