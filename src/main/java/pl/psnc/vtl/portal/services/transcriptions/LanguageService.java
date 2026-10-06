package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.model.transcriptions.Language;
import pl.psnc.vtl.portal.repositories.transcriptions.LanguageRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LanguageService {

    private final LanguageRepository languageRepository;

    public List<Language> getLanguages() {
        return languageRepository.findAllByForTranscriptionMetadataIsTrueOrderByOrd();
    }

}
