package pl.psnc.vtl.portal.repositories.iiiftools;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.repositories.statics.StaticFilesRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeleteTranscriptionImagesMailBuilder {

    private final StaticFilesRepository staticFilesRepository;

    private final String EMAIL_CONTENT = "email-error-while-deleting-transcription-files.html";


    public String makeEmailWithTranscriptionId(long transcriptionId, List<String> filenames) {
        if (filenames == null || filenames.isEmpty()) {
            return String.format(loadFIleContent(), transcriptionId, "wszystkich", "");
        } else {
            return String.format(loadFIleContent(), transcriptionId, "następujących", String.join(", ", filenames));
        }
    }


    private String loadFIleContent() {
            return staticFilesRepository.loadFileContent(EMAIL_CONTENT);
    }
}
