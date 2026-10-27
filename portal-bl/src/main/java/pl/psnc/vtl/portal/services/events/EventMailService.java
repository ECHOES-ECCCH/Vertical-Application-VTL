package pl.psnc.vtl.portal.services.events;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.conf.datetime.ApiDateTimeFormatter;
import pl.psnc.vtl.portal.model.events.Event;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.repositories.statics.StaticFilesRepository;
import pl.psnc.vtl.portal.services.mail.MailSender;

import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

@Service
public class EventMailService extends MailSender {

    private final StaticFilesRepository staticFilesRepository;

    public EventMailService(JavaMailSender emailSender, StaticFilesRepository staticFilesRepository) {
        super(emailSender);
        this.staticFilesRepository = staticFilesRepository;
    }

    public void sendInvitationEmail(Event event, User targetUser, String originUrl) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        // Title is the same for both languages, so use en locale
        simpleMailMessage.setSubject(ResourceBundle.getBundle("messages", new Locale("en"))
                .getString("event.email.invitation.title")
                .replace("{1}", event.getName()));
        String eventUrl = originUrl + "/event/" + event.getId();
        simpleMailMessage.setText(staticFilesRepository.loadFileContent("email-event-invite.html")
                .replace("${userName}", targetUser.getName())
                .replace("${eventName}", event.getName())
                .replace("${profileUrl}", originUrl + "/profile")
                .replace("${eventUrl}", eventUrl)
                .replace("${confirmUrl}", eventUrl + "?decision=confirm")
                .replace("${resignUrl}", eventUrl + "?decision=resign")
                .replace("${startTime}", ApiDateTimeFormatter.formatDateTime(event.getStartDate())));
        simpleMailMessage.setTo(targetUser.getEmail());
        simpleMailMessage.setFrom(emailAddress);
        simpleMailMessage.setSentDate(new Date());
        sendHTML(simpleMailMessage);
    }

    public void sendEmailToAttendees(Event event, String title, String message, String originUrl) {
        String eventUrl = originUrl + "/event/" + event.getId();
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        simpleMailMessage.setSubject(messageTitle(title));
        simpleMailMessage.setText(messageContent(event, eventUrl, message));
        simpleMailMessage.setTo(messageRecipients(event));
        simpleMailMessage.setFrom(emailAddress);
        simpleMailMessage.setSentDate(new Date());
        sendHTML(simpleMailMessage);
    }

    private static String[] messageRecipients(Event event) {
        return event.getAttendees().stream().map(
                eventAttendee -> eventAttendee.getUser().getEmail()).toArray(String[]::new);
    }

    private String messageTitle(String title) {
        return ResourceBundle.getBundle("messages", new Locale("en"))
                .getString("event.email.message.title")
                .replace("{1}", title);
    }

    private String messageContent(Event event, String eventUrl, String message) {
        Node markupDoc = Parser.builder().build().parse(message);
        String htmlRender = HtmlRenderer.builder().build().render(markupDoc);
        return ResourceBundle.getBundle("messages", new Locale("en"))
                .getString("event.email.message.header")
                .replace("{1}", eventUrl)
                .replace("{2}", event.getName())
                + htmlRender;
    }

}
