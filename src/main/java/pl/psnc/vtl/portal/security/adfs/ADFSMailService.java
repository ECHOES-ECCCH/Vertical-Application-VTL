package pl.psnc.vtl.portal.security.adfs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.repositories.statics.StaticFilesRepository;
import pl.psnc.vtl.portal.services.mail.MailSender;

import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

@Slf4j
@Service
public class ADFSMailService extends MailSender {

    private final StaticFilesRepository staticFilesRepository;
    private final String EMAIL_TITLE = "auth.emailChanged.email.title";
    private final String MAIL_CONTENT_FILE = "email-adfs-email-changed.html";


    public ADFSMailService(JavaMailSender emailSender, StaticFilesRepository staticFilesRepository) {
        super(emailSender);
        this.staticFilesRepository = staticFilesRepository;
    }


    public void sendMailChangedEmail(String email) {
        log.debug("Sending ADFS email");
        this.sendHTML(constructEmail(email));
    }


    private SimpleMailMessage constructEmail(String email) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject(getRegistrationEmailSubject());
        simpleMailMessage.setText(getMailChangedEmailContent());
        simpleMailMessage.setTo(email);
        simpleMailMessage.setFrom(emailAddress);
        simpleMailMessage.setSentDate(new Date());
        log.debug("ADFS email subject: " + simpleMailMessage.getSubject() + ", text: " + simpleMailMessage.getText()
                + ", to: " + emailAddress + ", from: " + simpleMailMessage.getFrom() + ", date: "
                + simpleMailMessage.getSentDate());
        return simpleMailMessage;
    }


    private String getMailChangedEmailContent() {
        return staticFilesRepository.loadFileContent(MAIL_CONTENT_FILE);
    }


    private String getRegistrationEmailSubject() {
        ResourceBundle messageBundle = ResourceBundle
                .getBundle("messages", new Locale(StaticFileLanguage.PL.getValue()));
        return messageBundle.getString(EMAIL_TITLE);
    }
}
