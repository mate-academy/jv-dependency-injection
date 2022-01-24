package mate.academy;

import mate.academy.lib.Injector;
import mate.academy.service.FileReaderService;

public class Main {

    public static void main(String[] args) {
        Injector injector = Injector.getInjector();
        injector.getInstance(FileReaderService.class);
    }
}
