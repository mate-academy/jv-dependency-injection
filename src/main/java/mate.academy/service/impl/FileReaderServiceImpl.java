package mate.academy.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import mate.academy.lib.Component;
import mate.academy.service.FileReaderService;

@Component
public class FileReaderServiceImpl implements FileReaderService {
    @Override
    public List<String> readFile(String filePath) {
        try {
            return Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Can't read file by path " + filePath, e);
        }
    }
}
