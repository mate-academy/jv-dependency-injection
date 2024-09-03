package mate.academy.service;

import mate.academy.model.Product;

public interface ProductParser {
    Product parse(String line);
}
