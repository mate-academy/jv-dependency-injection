package mate.academy.service;

import mate.academy.lib.Component;

@Component
public interface FileReaderService {
    List<String> readFile(String fileName);
}
