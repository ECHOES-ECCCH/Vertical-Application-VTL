package pl.psnc.vtl.iiif.eBooks;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MultipartFileGenerator {

    public MultipartFile generateMultipartFile(String filePath, String originalFileName) {
        Path path = Paths.get(filePath);
        String name = "file.txt";
        String contentType = "text/plain";
        byte[] content = null;
        try {
            content = Files.readAllBytes(path);
        } catch (final IOException e) {
        }
        MultipartFile result = new MockMultipartFile(name, originalFileName, contentType, content);

        return result;
    }
}
