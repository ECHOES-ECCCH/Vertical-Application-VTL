package pl.psnc.vtl.portal.services.registration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import pl.psnc.vtl.portal.dto.registration.ResetPasswordRequest;
import pl.psnc.vtl.portal.model.registration.PersistenceToken;
import pl.psnc.vtl.portal.model.registration.TokenType;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.repositories.users.UserRepository;

import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ResetPasswordServiceTest {

    @Mock
    PersistenceTokenProvider persistenceTokenProvider;

    @Mock
    ResetPasswordEmailService resetPasswordEmailService;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ResetPasswordService resetPasswordService;
    private ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();

    @Before
    public void init(){
        String EMAIL = "email";
        String redirectUr = "url";
        resetPasswordRequest.setEmail(EMAIL);
        resetPasswordRequest.setLang(StaticFileLanguage.EN);
        resetPasswordRequest.setClientRedirectUrl(redirectUr);
    }
    @Test
    public void SendMailWhenUserExist(){
        User user = new User();
        PersistenceToken persistenceToken = new PersistenceToken();
        EmailData emailData = new EmailData(user, persistenceToken, resetPasswordRequest.getClientRedirectUrl(), resetPasswordRequest.getLang());

        doReturn(Optional.of(user)).when(userRepository).findByEmail(resetPasswordRequest.getEmail());
        doReturn(persistenceToken).when(persistenceTokenProvider).createToken(user, TokenType.RESET_PASSWORD_TOKEN);
        doNothing().when(resetPasswordEmailService).sendEmail(any());

        resetPasswordService.sendEmailToResetPassword(resetPasswordRequest);

        verify(resetPasswordEmailService).sendEmail(any());
        verify(persistenceTokenProvider).createToken(user, TokenType.RESET_PASSWORD_TOKEN);
        verify(userRepository).findByEmail(resetPasswordRequest.getEmail());
    }

    @Test(expected = EmailDoesNotExistException.class)
    public void shouldThrowExceptionWhenEmailDoesntExist(){
        doThrow(EmailDoesNotExistException.class).when(userRepository).findByEmail(resetPasswordRequest.getEmail());

        resetPasswordService.sendEmailToResetPassword(resetPasswordRequest);

        verify(userRepository).findByEmail(resetPasswordRequest.getEmail());
    }

}