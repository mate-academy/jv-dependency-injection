package mate.academy.service;

import mate.academy.lib.Component;

import java.util.List;

@Component
public interface FileReaderService {
    List<String> readFile(String fileName);
}
