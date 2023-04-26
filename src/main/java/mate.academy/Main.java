package mate.academy;

import java.util.List;
import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.GetListProducts;

public class Main {
    public static void main(String[] args) {
        Injector injector = Injector.getInjector();
        GetListProducts getListProduct =
                (GetListProducts) injector.getInstance(GetListProducts.class);
        List<Product> products = getListProduct.getListProduct("products.txt");
        products.forEach(System.out::println);
    }
}
