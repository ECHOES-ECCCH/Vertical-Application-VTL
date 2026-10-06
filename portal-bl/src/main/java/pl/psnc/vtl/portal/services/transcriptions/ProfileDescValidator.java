package pl.psnc.vtl.portal.services.transcriptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import pl.psnc.vtl.portal.dto.transcriptions.ProfileDescCore;
import pl.psnc.vtl.portal.model.transcriptions.ProfileDesc;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;

import javax.transaction.Transactional;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProfileDescValidator {

    private static final int MAX_LINK_LENGTH = 2048;

    public ProfileDesc validate(ProfileDescCore profileDescCore, Transcription transcription, Errors errors) {
        ProfileDesc profileDesc = getOrCreateProfileDesc(transcription);
        List<String> keywords = new ArrayList<>();
        if (profileDescCore.getKeywords() != null) {
            for (int i = 0; i < profileDescCore.getKeywords().size(); i++) {
                if (StringUtils.isNotBlank(profileDescCore.getKeywords().get(i))) {
                    keywords.add(profileDescCore.getKeywords().get(i));
                }
            }
        }
        profileDesc.setKeywords(keywords);

        profileDesc.setSummary(profileDescCore.getSummary());

        List<String> links = new ArrayList<>();
        if (profileDescCore.getLinks() != null) {
            for (int i = 0; i < profileDescCore.getLinks().size(); i++) {
                String link = profileDescCore.getLinks().get(i);
                if (StringUtils.isNotBlank(link)) {
                    if(link.length() > MAX_LINK_LENGTH){
                        if (errors != null) {
                            errors.rejectValue("links[" + i + "]", "transcriptions.error." + TranscriptionErrorMessages.URL_TOO_LONG
                                    .getMessage());
                        }
                    } else {
                        try {
                            new URL(link);
                            links.add(link);
                        } catch (MalformedURLException e) {
                            if (errors != null) {
                                errors.rejectValue("links[" + i + "]",
                                        "transcriptions.error." + TranscriptionErrorMessages.MALFORMED_URL.getMessage());
                            }
                        }
                    }
                }
            }
        }
        profileDesc.setLinks(links);
        return profileDesc;
    }

    private ProfileDesc getOrCreateProfileDesc(Transcription transcription) {
        ProfileDesc profileDesc = transcription.getProfileDesc();
        if (profileDesc == null) {
            profileDesc = new ProfileDesc();
        }
        return profileDesc;
    }

}
