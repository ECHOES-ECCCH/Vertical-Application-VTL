package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import pl.psnc.vtl.portal.dto.transcriptions.MonographicInfoCore;
import pl.psnc.vtl.portal.model.transcriptions.MonographicInfo;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.services.ValidationException;

import javax.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MonographicInfoValidator {

    private final BiblTypeValidator biblTypeValidator;

    private final ActorValidator actorValidator;

    private final LanguageValidator languageValidator;

    public MonographicInfo validate(MonographicInfoCore monographicInfoCore, Transcription transcription ) {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(monographicInfoCore, "MonographicInfoCore");
        MonographicInfo monographicInfo = this.validate(monographicInfoCore, transcription, errors);
        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        } else {
            return monographicInfo;
        }
    }
    public MonographicInfo validate(MonographicInfoCore monographicInfoCore, Transcription transcription, Errors errors) {
        MonographicInfo monographicInfo = getOrCreateMonographicInfo(transcription);
        monographicInfo.setTitle(monographicInfoCore.getTitle());
        if (errors != null) {
            errors.pushNestedPath("biblType");
        }
        monographicInfo.setBiblType(biblTypeValidator.validate(monographicInfoCore.getBiblType(), errors));
        if (errors != null) {
            errors.popNestedPath();
        }
        monographicInfo.setActors(actorValidator.validate(monographicInfoCore.getActors(), errors, "actors"));
        if (errors != null) {
            errors.pushNestedPath("language");
        }
        monographicInfo.setLanguage(languageValidator.validate(monographicInfoCore.getLanguage(), errors));
        if (errors != null) {
            errors.popNestedPath();
        }
        monographicInfo.setSignature(monographicInfoCore.getSignature());
        monographicInfo.setChapterNo(monographicInfoCore.getChapterNo());
        monographicInfo.setVolumeNo(monographicInfoCore.getVolumeNo());
        monographicInfo.setIssueNo(monographicInfoCore.getIssueNo());
        monographicInfo.setPageRange(monographicInfoCore.getPageRange());
        monographicInfo.setPages(monographicInfoCore.getPages());
        monographicInfo.setPublisher(monographicInfoCore.getPublisher());
        monographicInfo.setPublicationPlace(monographicInfoCore.getPublicationPlace());
        monographicInfo.setPublicationDate(monographicInfoCore.getPublicationDate());
        monographicInfo.setIsbn(monographicInfoCore.getIsbn());
        monographicInfo.setEisbn(monographicInfoCore.getEisbn());
        monographicInfo.setIssn(monographicInfoCore.getIssn());
        monographicInfo.setEissn(monographicInfoCore.getEissn());
        return monographicInfo;
    }

    private MonographicInfo getOrCreateMonographicInfo(Transcription transcription) {
        MonographicInfo monographicInfo = transcription.getMonographicInfo();
        if (monographicInfo == null) {
            monographicInfo = new MonographicInfo();
        }
        return monographicInfo;
    }

}
