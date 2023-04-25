package mate.academy.service;

import java.util.List;
import mate.academy.lib.Inject;
import mate.academy.model.Product;

public class GetListProduct {
    @Inject
    private ProductService productService;

    public List<Product> getListProduct(String filePath) {
        return productService.getAllFromFile(filePath);
    }
}
