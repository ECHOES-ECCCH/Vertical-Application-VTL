package pl.psnc.vtl.portal.services.users;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.psnc.vtl.portal.TestAuthenticationService;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.model.users.SecurityGroup;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.repositories.users.UserRepository;
import pl.psnc.vtl.portal.security.CustomUser;
import pl.psnc.vtl.portal.security.adfs.ADFSAuthenticationToken;
import pl.psnc.vtl.portal.services.registration.CaptchaValidator;

import java.util.Collections;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class UsersCommunicationServiceTest {

    @Mock
    CaptchaValidator captchaValidator;
    @Mock
    UserRepository userRepository;

    @Mock
    UserCommunicationMailSender userCommunicationMailSender;
    @InjectMocks
    UsersCommunicationService communicationService;

    @Test
    public void shouldSendMail() {
        //given
        String senderEmail = "email";
        StaticFileLanguage language = StaticFileLanguage.PL;
        TestAuthenticationService.authenticateUser(3L, senderEmail);
        //and
        Long receiverId = 1L;
        String subject = "subject";
        String message = "message";
        String captcha = "captcha";
        UsersCommunicationMail dto = new UsersCommunicationMail(receiverId, subject, message, captcha);
        //and
        User receiver = User.builder().email("email").build();
        //and

        Mockito.doReturn(Optional.of(receiver)).when(userRepository).findById(receiverId);
        Mockito.doReturn(true).when(captchaValidator).isValidCaptcha(captcha);
        Mockito.doNothing().when(userCommunicationMailSender).sendEmail(subject, message, receiver.getEmail(), senderEmail,
                language);
        //when
        communicationService.sendEmail(dto, language);
        //then
        Mockito.verify(captchaValidator).isValidCaptcha(captcha);
        Mockito.verify(userCommunicationMailSender).sendEmail(subject, message, receiver.getEmail(), senderEmail,
                language);
    }

    private void authenticatedUser(long id, String email) {
        CustomUser user = new CustomUser(id, email, "password", true, false, Collections.singleton(SecurityGroup.USER));
        Authentication authentication = new ADFSAuthenticationToken(
                user, Collections.singleton(SecurityGroup.USER));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}