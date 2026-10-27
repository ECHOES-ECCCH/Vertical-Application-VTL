package pl.psnc.vtl.portal.services.registration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.services.mail.TokenizedMailBuilder;
import pl.psnc.vtl.portal.services.mail.MailSender;

import java.util.Locale;
import java.util.Date;
import java.util.ResourceBundle;

@Service
public class RegistrationEmailService extends MailSender {

    private final String EMAIL_TITLE = "auth.registration.email.title";
    private final TokenizedMailBuilder tokenizedMailBuilder;


    public RegistrationEmailService(JavaMailSender emailSender, @Qualifier("registration") TokenizedMailBuilder tokenizedMailBuilder) {
        super(emailSender);
        this.tokenizedMailBuilder = tokenizedMailBuilder;
    }

    public void sendEmail(EmailData emailData) {
        SimpleMailMessage simpleMailMessage = constructEmail(emailData);
        sendHTML(simpleMailMessage);
    }

    private SimpleMailMessage constructEmail(EmailData emailData) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject(getRegistrationEmailSubject(emailData.getLang().getValue()));
        simpleMailMessage.setText(makeRegistrationEmailBody(emailData));
        simpleMailMessage.setTo(emailData.getUser().getEmail());
        simpleMailMessage.setFrom(emailAddress);
        simpleMailMessage.setSentDate(new Date());
        return simpleMailMessage;
    }

    private String getRegistrationEmailSubject(String lang) {
        ResourceBundle messageBundle = ResourceBundle.getBundle("messages", new Locale(lang));
        return messageBundle.getString(EMAIL_TITLE);
    }


    private String makeRegistrationEmailBody(EmailData emailData) {
        return tokenizedMailBuilder.makeEmailWithTokenBody(emailData);
    }

}
