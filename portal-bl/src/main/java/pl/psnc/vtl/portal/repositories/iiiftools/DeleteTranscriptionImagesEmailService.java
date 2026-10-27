package pl.psnc.vtl.portal.repositories.iiiftools;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.services.mail.MailSender;

import java.util.List;
import java.util.ResourceBundle;

@Service
public class DeleteTranscriptionImagesEmailService extends MailSender {

    private final String EMAIL_TITLE = "transcriptions.deleteImages.email.title";

    private final DeleteTranscriptionImagesMailBuilder deleteTranscriptionImagesMailBuilder;

    public DeleteTranscriptionImagesEmailService(JavaMailSender javaMailSender, DeleteTranscriptionImagesMailBuilder deleteTranscriptionImagesMailBuilder) {
        super(javaMailSender);
        this.deleteTranscriptionImagesMailBuilder = deleteTranscriptionImagesMailBuilder;
    }


    public void sendEmail(long transcriptionId, List<String> filenames, String[] usersMails) {
        SimpleMailMessage simpleMailMessage = constructEmail(transcriptionId, filenames, usersMails);
        sendHTML(simpleMailMessage);
    }


    private SimpleMailMessage constructEmail(long transcriptionId, List<String> filenames, String[] userEmail) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject(getDeleteTranscriptionImagesEmailSubject());
        simpleMailMessage.setText(getDeleteTranscriptionImagesEmailContent(transcriptionId, filenames));
        simpleMailMessage.setTo(userEmail);
        simpleMailMessage.setFrom(emailAddress);
        return simpleMailMessage;
    }


    private String getDeleteTranscriptionImagesEmailSubject() {
        ResourceBundle messageBundle = ResourceBundle.getBundle("messages", LocaleContextHolder.getLocale());
        return messageBundle.getString(EMAIL_TITLE);
    }


    private String getDeleteTranscriptionImagesEmailContent(long transcriptionId, List<String> filenames) {
        return deleteTranscriptionImagesMailBuilder.makeEmailWithTranscriptionId(transcriptionId, filenames);
    }
}
