package mate.academy;

import mate.academy.lib.Injector;
import mate.academy.model.Product;
import mate.academy.service.ProductParser;

public class Main {

    public static void main(String[] args) {
        ProductParser parser = (ProductParser) Injector.getInstance(ProductParser.class);

        Product product = parser.parse("1,Apple,Food,Red apple,15.75");

        System.out.println(product.getName());
        System.out.println(product.getPrice());
    }
}
