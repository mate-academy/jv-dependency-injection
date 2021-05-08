package mate.academy.service;

import java.util.List;

public interface FileReaderService {
    @Component
    List<String> readFile(String fileName);
}
