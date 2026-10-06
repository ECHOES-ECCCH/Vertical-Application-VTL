package pl.psnc.vtl.portal.services.registration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.repositories.statics.StaticFilesRepository;
import pl.psnc.vtl.portal.services.mail.TokenizedMailBuilder;

@Service
@Qualifier("federationRegistration")
@RequiredArgsConstructor
public class FederationRegistrationMailBuilder implements TokenizedMailBuilder {

    private final StaticFilesRepository staticFilesRepository;

    private static final String ENG_EMAIL_FILE_NAME = "email-federation-registration-en.html";

    private static final String PL_EMAIL_FILE_NAME = "email-federation-registration-pl.html";

    private static final String DE_EMAIL_FILE_NAME = "email-federation-registration-de.html";

    private static final String CLIENT_PLACEHOLDER = "${client}";

    @Override
    public String makeEmailWithTokenBody(EmailData emailData) {
        String emailContent = loadFIleContent(emailData.getLang());
        return emailContent.replace(CLIENT_PLACEHOLDER, ((FederationEmailData) emailData).getClient());
    }


    private String loadFIleContent(StaticFileLanguage lang) {
        switch (lang) {
            case EN:
                return staticFilesRepository.loadFileContent(ENG_EMAIL_FILE_NAME);
            case DE:
                return staticFilesRepository.loadFileContent(DE_EMAIL_FILE_NAME);
            default:
                return staticFilesRepository.loadFileContent(PL_EMAIL_FILE_NAME);
        }
    }
}
