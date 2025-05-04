package mate.academy;

import mate.academy.app.ProductApp;
import mate.academy.lib.Injector;

public class Main {
    public static void main(String[] args) {
        Injector injector = Injector.getInjector();
        ProductApp productApp = (ProductApp) injector.getInstance(ProductApp.class);
        productApp.createListOfProducts();
    }
}
