package pl.psnc.vtl.portal.services.users;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.repositories.users.UserRepository;
import pl.psnc.vtl.portal.services.registration.CaptchaValidator;
import pl.psnc.vtl.portal.services.registration.LoggedInUserHolder;

import javax.persistence.EntityNotFoundException;

@RequiredArgsConstructor
@Service
public class UsersCommunicationService {

    private final CaptchaValidator captchaValidator;

    private final UserRepository userRepository;

    private final UserCommunicationMailSender userCommunicationMailSender;

    private  final LoggedInUserHolder loggedInUserHolder;

    public void sendEmail(UsersCommunicationMail dto, StaticFileLanguage language) {
        String receiverEmail = userRepository.findById(dto.getReceiverId())
                .map(User::getEmail)
                .orElseThrow(EntityNotFoundException::new);
        if (!captchaValidator.isValidCaptcha(dto.getCaptcha()))
            throw new IllegalArgumentException("wrong captcha");
        String senderEmail = loggedInUserHolder.getLoggedInUser().getEmail();
        userCommunicationMailSender.sendEmail(dto.getSubject(), dto.getMessage(), receiverEmail, senderEmail, language);
    }
}
