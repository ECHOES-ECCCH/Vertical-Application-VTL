package pl.psnc.vtl.portal.services.mail;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
public abstract class MailSender {

    private final JavaMailSender emailSender;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Value("${spring.mail.address}")
    protected String emailAddress;

    @SneakyThrows
    protected void sendHTML(SimpleMailMessage simpleMailMessage) {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
        helper.setSubject(simpleMailMessage.getSubject());
        helper.setFrom(simpleMailMessage.getFrom());
        if (simpleMailMessage.getTo().length > 1) {
            helper.setBcc(simpleMailMessage.getTo());
        } else {
            helper.setTo(simpleMailMessage.getTo());
        }
        helper.setText(simpleMailMessage.getText(), true);

        executor.submit(() -> {
            log.debug("Sending email (html) TO: "  + simpleMailMessage.getTo()[0] + " subject: "+ simpleMailMessage.getSubject());
            try {
                emailSender.send(message);
            } catch (Exception e) {
                log.error("Error sending email", e);
            }
        });

    }


    protected void send(SimpleMailMessage simpleMailMessage) {
        log.debug("Sending email (simpleMailMessage) to: "  + simpleMailMessage.getTo().toString() + " subject: "+ simpleMailMessage.getSubject());
        executor.submit(() -> emailSender.send(simpleMailMessage));
    }

}

