package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.users.QuotaException;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionRepository;
import pl.psnc.vtl.portal.services.ValidationException;

import javax.persistence.EntityNotFoundException;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TranscriptionValidator {

    private final TranscriptionRepository transcriptionRepository;

    private final AnalyticInfoValidator analyticInfoValidator;

    private final MonographicInfoValidator monographicInfoValidator;
    private final TranscriptionInfoValidator transcriptionInfoValidator;

    private final ProfileDescValidator profileDescValidator;


    public Transcription validate(TranscriptionCore transcriptionCore, Long transcriptionId, boolean skipMetadataValidation)
            throws ValidationException {
        Transcription transcription = getOrCreateTranscription(transcriptionId);
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(transcriptionCore, "Transcription");

        errors.pushNestedPath("analyticInfo");
        transcription.setAnalyticInfo(
                analyticInfoValidator.validate(transcriptionCore.getAnalyticInfo(), transcription, errors));
        errors.popNestedPath();

        errors.pushNestedPath("monographicInfo");
        transcription.setMonographicInfo(
                monographicInfoValidator.validate(transcriptionCore.getMonographicInfo(), transcription, errors));
        errors.popNestedPath();

        errors.pushNestedPath("transcriptionInfo");
        transcription.setTranscriptionInfo(
                transcriptionInfoValidator.validate(transcriptionCore.getTranscriptionInfo(), transcription, errors));
        errors.popNestedPath();

        errors.pushNestedPath("profileDesc");
        transcription.setProfileDesc(
                profileDescValidator.validate(transcriptionCore.getProfileDesc(), transcription, errors));
        errors.popNestedPath();

        if (StringUtils.isBlank(transcription.getAnalyticInfo().getTitle()) && StringUtils.isBlank(
                transcription.getMonographicInfo().getTitle())) {
            errors.rejectValue("analyticInfo.title",
                    "transcriptions.error." + TranscriptionErrorMessages.AT_LEAST_ONE_TITLE_IS_REQUIRED.getMessage());
            errors.rejectValue("monographicInfo.title",
                    "transcriptions.error." + TranscriptionErrorMessages.AT_LEAST_ONE_TITLE_IS_REQUIRED.getMessage());
        }

        if (transcriptionId == null) {
            if (transcriptionCore.getAcceptedRegulations() == null || !transcriptionCore.getAcceptedRegulations()) {
                errors.rejectValue("acceptedRegulations",
                        "transcriptions.error." + TranscriptionErrorMessages.FIELD_REQUIRED.getMessage());
            }
        }

        if (errors.hasErrors() && !skipMetadataValidation) {
            throw new ValidationException(errors);
        } else {
            return transcription;
        }
    }

    private Transcription getOrCreateTranscription(Long transcriptionId) {
        if (transcriptionId != null) {
            return transcriptionRepository.getOne(transcriptionId);
        } else {
            return new Transcription();
        }
    }

    @Transactional(readOnly = true)
    public void validateUsedTranscriptionSpaceQuota(Long transcriptionId) {
        Transcription transcription = transcriptionRepository.findById(transcriptionId).orElseThrow(
                () -> new EntityNotFoundException("Unable to find " + Transcription.class.getName() + " with id '" + transcriptionId + "'!"));
        if(transcription.getQuota() < transcription.getUsedSpaceQuota()) {
            throw new QuotaException("Transcription Quota exceeded error", "quota.error.translation.exceeded");
        }
    }

}
