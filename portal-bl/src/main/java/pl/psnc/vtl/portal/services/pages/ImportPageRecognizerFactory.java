package pl.psnc.vtl.portal.services.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.controllers.pages.TranscriptionPageContentType;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportPageRecognizerFactory {

    private final List<ImportPageRecognizer> importPageRecognizers;

    public ImportPageRecognizer provideImportPageRecognizer(TranscriptionPageContentType transcriptionPageContentType) {
        for (ImportPageRecognizer importPageRecognizer : importPageRecognizers) {
            if (importPageRecognizer.supports(transcriptionPageContentType)) {
                return importPageRecognizer;
            }
        }
        return null;
    }

}
