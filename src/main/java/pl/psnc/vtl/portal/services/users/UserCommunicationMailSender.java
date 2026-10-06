package pl.psnc.vtl.portal.services.users;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.services.mail.MailSender;

import java.util.Locale;
import java.util.ResourceBundle;

@Service
class UserCommunicationMailSender extends MailSender {
    UserCommunicationMailSender(JavaMailSender emailSender) {
        super(emailSender);
    }

    void sendEmail(String subject, String message, String receiver, String sender, StaticFileLanguage language) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(receiver);
        mail.setFrom(this.emailAddress);
        mail.setReplyTo(sender);
        mail.setSubject(getSubjectPrefix(language) + " " + subject);
        mail.setText(String.format(getTextPrefix(language), sender) + "\n" + message);
        send(mail);
    }

    private String getSubjectPrefix(StaticFileLanguage language) {
        ResourceBundle messageBundle = ResourceBundle.getBundle("messages",  new Locale(language.getValue()));
        return messageBundle.getString("email.subject.prefix");
    }

    private String getTextPrefix(StaticFileLanguage language) {
        ResourceBundle messageBundle = ResourceBundle.getBundle("messages",  new Locale(language.getValue()));
        return messageBundle.getString("email.content.prefix");
    }

}
