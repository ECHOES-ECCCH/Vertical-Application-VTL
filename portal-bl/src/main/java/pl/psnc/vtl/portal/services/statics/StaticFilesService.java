package pl.psnc.vtl.portal.services.statics;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.dto.statics.StaticFile;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.repositories.statics.StaticFilesRepository;

import java.io.IOException;
import java.nio.file.Files;

@Service
@RequiredArgsConstructor
public class StaticFilesService{

    private final StaticFilesRepository repository;

    public StaticFile loadFile(String fileName) {
        StaticFile staticFile = new StaticFile();
        staticFile.setEn(repository.loadFileContent(getFullFileName(fileName, StaticFileLanguage.EN)));
        staticFile.setPl(repository.loadFileContent(getFullFileName(fileName, StaticFileLanguage.PL)));
        staticFile.setDe(repository.loadFileContent(getFullFileName(fileName, StaticFileLanguage.DE)));
        return staticFile;
    }

    public Resource loadImage(String fileName) {
        return new FileSystemResource(repository.getFullPath(fileName));
    }

    public MediaType getMediaType(String fileName) {
        try {
            return MediaType.parseMediaType(Files.probeContentType(repository.getFullPath(fileName)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getFullFileName(String fileName, StaticFileLanguage lang) {
        return fileName + "-" + lang + ".md";
    }
}
