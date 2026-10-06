package pl.psnc.vtl.portal.repositories.statics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Repository
public class StaticFilesRepository {

    @Value("${vtl.conf-dir}")
    private String filesLocation;


    public String loadFileContent(String fullFileName) {
        try {
            return new String(Files.readAllBytes(getFullPath(fullFileName)));
        } catch (IOException e) {
            throw new StaticFileNotFoundException(fullFileName);
        }
    }

    public Path getFullPath(String fullFileName) {
        return Paths.get(filesLocation + fullFileName);
    }

}
