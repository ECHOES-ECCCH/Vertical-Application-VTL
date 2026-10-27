package pl.psnc.vtl.portal.services.transcriptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUser;
import pl.psnc.vtl.portal.model.permissions.TranscriptionUserId;
import pl.psnc.vtl.portal.repositories.statics.StaticFilesRepository;
import pl.psnc.vtl.portal.repositories.transcriptions.TranscriptionUserRepository;
import pl.psnc.vtl.portal.services.mail.MailSender;

import java.util.*;


@Slf4j
@Service
public class TranscriptionPrivilegesMailService extends MailSender {

    private final StaticFilesRepository staticFilesRepository;

    private final TranscriptionUserRepository transcriptionUserRepository;

    public TranscriptionPrivilegesMailService(JavaMailSender emailSender, StaticFilesRepository staticFilesRepository,
                                              TranscriptionUserRepository transcriptionUserRepository) {
        super(emailSender);
        this.staticFilesRepository = staticFilesRepository;
        this.transcriptionUserRepository = transcriptionUserRepository;
    }

    private SimpleMailMessage constructEmail(
            TranscriptionUser transcriptionUser, String originUrl) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        // Label is the same for both languages, so use en locale
        simpleMailMessage.setSubject(ResourceBundle.getBundle("messages", new Locale("en"))
                .getString("transcriptions.email.rightsGranted.title")
                .replace("{1}", transcriptionUser.getTranscription().getTitleLabel()));
        simpleMailMessage.setText(getEmailBody(transcriptionUser, originUrl));
        simpleMailMessage.setTo(transcriptionUser.getUser().getEmail());
        simpleMailMessage.setFrom(emailAddress);
        simpleMailMessage.setSentDate(new Date());
        return simpleMailMessage;
    }

    public void sendEmail(TranscriptionUserId transcriptionUserId, String originUrl) {
        TranscriptionUser transcriptionUser = transcriptionUserRepository.findById(transcriptionUserId).orElse(null);
        if (transcriptionUser == null)
            return; // Transcription right already removed
        SimpleMailMessage simpleMailMessage = constructEmail(transcriptionUser, originUrl);
        sendHTML(simpleMailMessage);
    }

    public String getEmailBody(TranscriptionUser transcriptionUser, String originUrl) {
        String emailContent = staticFilesRepository.loadFileContent("email-transcription-rights.html");
        String transcriptionUrl = originUrl + "/transcription/" + transcriptionUser.getTranscription().getId();
        return emailContent
                .replace("${transcriptionName}", transcriptionUser.getTranscription().getTitleLabel())
                .replace("${transcriptionUrl}", transcriptionUrl)
                .replace("${rolePl}", transcriptionUser.getRole().getPl())
                .replace("${roleEn}", transcriptionUser.getRole().getEn());
    }

}
