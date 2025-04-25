package services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileService {
    private String directory;

    public FileService(String directory) {
        this.directory = directory;
    }

    public byte[] readFile(String fileName) throws IOException {
        Path filePath = Paths.get(this.directory, fileName);
        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            return Files.readAllBytes(filePath);

        } else {
            throw new NoSuchFileException("File not found: " + filePath);
        }
    }

    public void writeFile(String fileName, String content) throws IOException {
        Path filePath = Paths.get(this.directory, fileName);
        Files.write(filePath,content.getBytes());
    }

}
