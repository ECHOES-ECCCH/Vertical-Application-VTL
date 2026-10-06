package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionInfoCore;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.transcriptions.TranscriptionInfo;
import pl.psnc.vtl.portal.services.ValidationException;
import pl.psnc.vtl.portal.services.users.EmailValidator;

import javax.transaction.Transactional;

import static pl.psnc.vtl.portal.services.transcriptions.TranscriptionInfoErrorMessages.INVALID_CONTACT_EMAIL;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TranscriptionInfoValidator {


    public TranscriptionInfo validate(TranscriptionInfoCore transcriptionInfoCore, Transcription transcription ) {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(transcriptionInfoCore, "TranscriptionInfo");
        TranscriptionInfo transcriptionInfo = this.validate(transcriptionInfoCore, transcription, errors);
        if (errors.hasErrors()) {
            throw new ValidationException(errors);
        } else {
            return transcriptionInfo;
        }

    }
    public TranscriptionInfo validate(TranscriptionInfoCore transcriptionInfoCore, Transcription transcription,
                                      Errors errors) {

        TranscriptionInfo transcriptionInfo = getOrCreateTranscriptionInfo(transcription);
        transcriptionInfo.setReleaseDate(transcriptionInfoCore.getReleaseDate());
        transcriptionInfo.setDescription(transcriptionInfoCore.getDescription());
        transcriptionInfo.setDocumentTypes(transcriptionInfoCore.getDocumentTypes());
        transcriptionInfo.setDocumentSrc(transcriptionInfoCore.getDocumentSrc());
        transcriptionInfo.setImgSrc(transcriptionInfoCore.getImgSrc());
        transcriptionInfo.setCollectionName(transcriptionInfoCore.getCollectionName());
        transcriptionInfo.setContactName(transcriptionInfoCore.getContactName());
        transcriptionInfo.setGuidelines(transcriptionInfoCore.getGuidelines());
        return transcriptionInfo;
    }

    private TranscriptionInfo getOrCreateTranscriptionInfo(Transcription transcription) {
        TranscriptionInfo transcriptionInfo = transcription.getTranscriptionInfo();
        if (transcriptionInfo == null) {
            transcriptionInfo = new TranscriptionInfo();
        }
        return transcriptionInfo;
    }

}
