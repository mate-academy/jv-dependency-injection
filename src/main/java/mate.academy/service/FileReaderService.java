package mate.academy.service;

import java.util.List;
import mate.academy.lib.Component;

public interface FileReaderService {
    List<String> readFile(String fileName);
}
