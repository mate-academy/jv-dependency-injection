package mate.academy.service;

import java.util.List;

@Component
public interface FileReaderService {
    List<String> readFile(String fileName);
}
