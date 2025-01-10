package mate.academy.service;

import mate.academy.lib.Component;
import java.util.List;
import mate.academy.model.Product;

@Component
public interface ProductService {
    List<Product> getAllFromFile(String filePath);
}
