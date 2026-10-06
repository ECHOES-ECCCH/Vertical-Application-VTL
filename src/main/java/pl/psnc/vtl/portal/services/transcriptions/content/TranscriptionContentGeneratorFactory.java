package pl.psnc.vtl.portal.services.transcriptions.content;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;

import java.util.List;

@Service
@AllArgsConstructor
public class TranscriptionContentGeneratorFactory {

    private List<TranscriptionContentGenerator> transcriptionContentGenerators;

    public TranscriptionContentGenerator provideGenerator(TranscriptionPageContentType transcriptionPageContentType) {
        for (TranscriptionContentGenerator transcriptionContentGenerator : transcriptionContentGenerators) {
            if (transcriptionContentGenerator.supports(transcriptionPageContentType)) {
                return transcriptionContentGenerator;
            }
        }
        throw new UnsupportedOperationException(
                "There is no TranscriptionContentGenerator in the context suitable for given transcription page format");
    }
}
