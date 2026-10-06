package pl.psnc.vtl.portal.services.transcriptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.repositories.statics.StaticFilesRepository;
import pl.psnc.vtl.portal.services.mail.MailSender;

import java.util.Date;

@Slf4j
@Service
public class TranscriptionExportMailService extends MailSender {

    private final StaticFilesRepository staticFilesRepository;

    @Value("${export.export-rejected-email-title}")
    private String exportRejectedEmailTitle;

    public TranscriptionExportMailService(JavaMailSender emailSender, StaticFilesRepository staticFilesRepository) {
        super(emailSender);
        this.staticFilesRepository = staticFilesRepository;
    }

    public void sendEmail(Transcription transcription, String message, String toEmail) {
        String emailContent = staticFilesRepository.loadFileContent("email-transcription-export-rejected.html");
        emailContent = emailContent.replace("${transcriptionTitle}", transcription.getAnalyticInfo().getTitle()).replace("${message}", message);

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject(exportRejectedEmailTitle);
        simpleMailMessage.setText(emailContent);
        simpleMailMessage.setTo(toEmail);
        simpleMailMessage.setFrom(emailAddress);
        simpleMailMessage.setSentDate(new Date());

        send(simpleMailMessage);
    }
}
