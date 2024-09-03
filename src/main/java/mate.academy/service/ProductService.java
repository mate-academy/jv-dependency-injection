package mate.academy.service;

import java.util.List;
import mate.academy.model.Product;

public interface ProductService {
    List<Product> getAllProducts();

    List<Product> getAllFromFile(String fileName);
}
