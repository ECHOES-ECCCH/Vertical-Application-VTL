package pl.psnc.vtl.portal.services.registration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.repositories.statics.StaticFilesRepository;
import pl.psnc.vtl.portal.services.mail.TokenizedMailBuilder;

@Service
@Qualifier("passwordReset")
@RequiredArgsConstructor
public class PasswordResetMailBuilder implements TokenizedMailBuilder {

    private final StaticFilesRepository staticFilesRepository;

    private static final String ENG_EMAIL_FILE_NAME = "email-reset-password-en.html";

    private static final String PL_EMAIL_FILE_NAME = "email-reset-password-pl.html";

    private static final String DE_EMAIL_FILE_NAME = "email-reset-password-de.html";

    private static final String TOKEN_PLACEHOLDER = "${token}";


    @Override
    public String makeEmailWithTokenBody(EmailData emailData) {
        String emailContent = loadFileContent(emailData.getLang());
        String clientRedirectUrl = emailData.getClientRedirectURL().concat(emailData.getPersistenceToken().getToken());
        return emailContent.replace(TOKEN_PLACEHOLDER, clientRedirectUrl);
    }


    private String loadFileContent(StaticFileLanguage lang) {
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
