package mate.academy.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import mate.academy.lib.Component;
import mate.academy.service.FileReaderService;

@Component
public class FileReaderServiceImpl implements FileReaderService {
    @Override
    public List<String> readFile(String fileName) {
        try {
            return Files.readAllLines(new File(fileName).toPath());
        } catch (IOException e) {
            throw new RuntimeException("Can't read file: " + fileName, e);
        }
    }
}
