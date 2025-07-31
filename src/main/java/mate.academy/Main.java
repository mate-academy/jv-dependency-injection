package mate.academy;

import mate.academy.lib.Injector;

public class Main {

    public static void main(String[] args) {
        // Please test your Injector here. Feel free to push this class as a part of your solution
        Injector injector = Injector.getInjector();
        // add this class only for check, that my injector works with classes too
        Controller controller = (Controller) injector.getInstance(Controller.class);
        controller.execute();
    }
}
