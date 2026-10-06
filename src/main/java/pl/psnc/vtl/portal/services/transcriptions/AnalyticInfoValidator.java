package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import pl.psnc.vtl.portal.dto.transcriptions.AnalyticInfoCore;
import pl.psnc.vtl.portal.model.transcriptions.AnalyticInfo;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;

import javax.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AnalyticInfoValidator {

    private final ActorValidator actorValidator;

    private final LanguageValidator languageValidator;


    public AnalyticInfo validate(AnalyticInfoCore analyticInfoCore, Transcription transcription, Errors errors) {
        AnalyticInfo analyticInfo = getOrCreateAnalyticInfo(transcription);
        analyticInfo.setTitle(analyticInfoCore.getTitle());
        analyticInfo.setActors(actorValidator.validate(analyticInfoCore.getActors(), errors, "actors"));
        if (errors != null) {
            errors.pushNestedPath("language");
        }
        analyticInfo.setLanguage(languageValidator.validate(analyticInfoCore.getLanguage(), errors));
        if (errors != null) {
            errors.popNestedPath();
        }
        return analyticInfo;
    }

    private AnalyticInfo getOrCreateAnalyticInfo(Transcription transcription) {
        AnalyticInfo analyticInfo = transcription.getAnalyticInfo();
        if (analyticInfo == null) {
            analyticInfo = new AnalyticInfo();
        }
        return analyticInfo;
    }

}
