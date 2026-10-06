package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionMetadataCore;
import pl.psnc.vtl.portal.model.transcriptions.*;
import pl.psnc.vtl.portal.services.ValidationException;

import javax.transaction.Transactional;

import java.util.ArrayList;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TranscriptionMetadataValidator {


//    private final AnalyticInfoValidator analyticInfoValidator;

    private final MonographicInfoValidator monographicInfoValidator;
    private final TranscriptionInfoValidator transcriptionInfoValidator;
    private final ProfileDescValidator profileDescValidator;
    private final AnalyticInfoValidator analyticInfoValidator;

    public Transcription validate(TranscriptionMetadataCore transcriptionMetadataCore, Transcription transcription) {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(transcriptionMetadataCore, "Metadata");
        if (transcriptionMetadataCore.getMonographicInfo() != null) {

            errors.pushNestedPath("monographicInfo");
            MonographicInfo mi = this.monographicInfoValidator.validate(transcriptionMetadataCore.getMonographicInfo(),
                    transcription, errors);
            //convert to mutable list
            mi.setActors(new ArrayList<>(mi.getActors()));
            transcription.setMonographicInfo(mi);
            errors.popNestedPath();
        }
        if (transcriptionMetadataCore.getTranscriptionInfo() != null) {

            errors.pushNestedPath("transcriptionInfo");
            TranscriptionInfo ti =
                    this.transcriptionInfoValidator.validate(transcriptionMetadataCore.getTranscriptionInfo(),
                            transcription, errors);
            transcription.setTranscriptionInfo(ti);


            errors.popNestedPath();
        }

        if (transcriptionMetadataCore.getAnalyticInfo() != null) {

            errors.pushNestedPath("analyticInfo");
            AnalyticInfo ai = this.analyticInfoValidator.validate(transcriptionMetadataCore.getAnalyticInfo(),
                    transcription, errors);
            //convert to mutable list
            ai.setActors(new ArrayList<>(ai.getActors()));
            transcription.setAnalyticInfo(ai);

            errors.popNestedPath();
        }

        if (transcriptionMetadataCore.getProfileDesc() != null) {

            errors.pushNestedPath("profileDesc");

            ProfileDesc pd = this.profileDescValidator.validate(transcriptionMetadataCore.getProfileDesc(),
                    transcription, errors);
            //convert to mutable list
            pd.setKeywords(new ArrayList<>(pd.getKeywords()));
            pd.setLinks(new ArrayList<>(pd.getLinks()));
            transcription.setProfileDesc(pd);
            errors.popNestedPath();

        }

        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        }
        return transcription;

    }


}
