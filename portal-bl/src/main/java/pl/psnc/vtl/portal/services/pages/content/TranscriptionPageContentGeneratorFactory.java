package pl.psnc.vtl.portal.services.pages.content;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class TranscriptionPageContentGeneratorFactory {

    private List<TranscriptionPageContentGenerator> transcriptionPageContentGenerators;

    public TranscriptionPageContentGenerator provideGenerator(TranscriptionPageContentType transcriptionPageContentType) {
        for (TranscriptionPageContentGenerator transcriptionPageContentGenerator : transcriptionPageContentGenerators) {
            if (transcriptionPageContentGenerator.supports(transcriptionPageContentType)) {
                return transcriptionPageContentGenerator;
            }
        }
        throw new UnsupportedOperationException(
                "There is no TranscriptionPageContentGenerator in the context suitable for given transcription page format");
    }
}