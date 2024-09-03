package mate.academy.service;

import mate.academy.lib.Component;

@Component
public interface ProductService {
    List<Product> getAllFromFile(String filePath);
}
