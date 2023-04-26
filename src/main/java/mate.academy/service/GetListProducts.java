package mate.academy.service;

import java.util.List;
import mate.academy.lib.Component;
import mate.academy.lib.Inject;
import mate.academy.model.Product;

@Component
public class GetListProducts {
    @Inject
    private ProductService productService;

    public List<Product> getListProduct(String filePath) {
        return productService.getAllFromFile(filePath);
    }
}
