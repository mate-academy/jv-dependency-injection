package mate.academy.service.impl;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import mate.academy.lib.Component;
import mate.academy.service.FileReaderService;

@Component
public class FileReaderServiceImpl implements FileReaderService {
    @Override
    public List<String> readFile(String fileName) {
        try {
            return Files.readAllLines(Paths.get(fileName));
        } catch (Exception e) {
            throw new RuntimeException("Can't read file: " + fileName, e);
        }
    }
}
