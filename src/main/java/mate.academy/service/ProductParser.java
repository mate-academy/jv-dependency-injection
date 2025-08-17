package mate.academy.service;

import java.util.List;
import mate.academy.model.Product;

public interface ProductParser {
    List<Product> parse(List<String> lines);
}
