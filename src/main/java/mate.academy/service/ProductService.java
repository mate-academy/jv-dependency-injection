package mate.academy.service;

import java.util.List;
import mate.academy.model.Product;

@Component
public interface ProductService {
    List<Product> getAllFromFile(String filePath);
}
