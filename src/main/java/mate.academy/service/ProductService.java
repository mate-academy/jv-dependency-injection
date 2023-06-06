package mate.academy.service;

import mate.academy.model.Product;
import java.util.List;

public interface ProductService {
    List<Product> getAllFromFile(String filePath);
}
