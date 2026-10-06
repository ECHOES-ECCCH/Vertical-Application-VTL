package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import pl.psnc.vtl.portal.dto.transcriptions.LanguageId;
import pl.psnc.vtl.portal.model.transcriptions.Language;
import pl.psnc.vtl.portal.repositories.transcriptions.LanguageRepository;

import javax.transaction.Transactional;
import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LanguageValidator {

    private final LanguageRepository languageRepository;


    public Language validate(LanguageId languageId, Errors errors) {
        if (languageId != null) {
            Optional<Language> languageHolder = languageRepository.findById(languageId.getCode());
            if (languageHolder.isEmpty()) {
                if (errors != null) {
                    errors.rejectValue("code", "transcriptions.error." + TranscriptionErrorMessages.FIELD_REQUIRED.getMessage());
                }
            } else {
                return languageHolder.get();
            }
        }
        return null;
    }

}
