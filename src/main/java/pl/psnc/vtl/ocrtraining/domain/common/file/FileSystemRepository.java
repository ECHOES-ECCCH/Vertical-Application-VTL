package pl.psnc.vtl.ocrtraining.domain.common.file;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;


@Component
public class FileSystemRepository {

    public void createDirectories(Path directoryPath) throws IOException {
        Files.createDirectories(directoryPath);
    }

    public Path writeBytesToFile(byte[] contents, Path destination) throws IOException {
        createDirectories(destination.getParent());
        return Files.write(destination, contents);
    }

    public void copyFile(Path source, Path destination) throws IOException {
        // try to make directories in cases where the process which copies data should do that
        createDirectories(destination.getParent());
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    public void deleteFile(Path filePath) throws IOException {
        Files.deleteIfExists(filePath);
    }

    public boolean fileExists(Path filePath) {
        return Files.exists(filePath);
    }

}
