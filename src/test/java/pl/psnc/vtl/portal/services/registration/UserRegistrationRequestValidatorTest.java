package pl.psnc.vtl.portal.services.registration;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import pl.psnc.vtl.portal.controllers.ErrorResponseMessage;
import pl.psnc.vtl.portal.controllers.RequestFieldsValidationException;
import pl.psnc.vtl.portal.dto.registration.UserRegisterCore;
import pl.psnc.vtl.portal.repositories.users.UserRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class UserRegistrationRequestValidatorTest {

    private final String VALID_CAPTCHA = "valid";
    private static final String INVALID_CAPTCHA = "invalid";

    @Before
    public void init(){
        doReturn(true).when(captchaValidator).isValidCaptcha(VALID_CAPTCHA);
        doReturn(false).when(captchaValidator).isValidCaptcha(INVALID_CAPTCHA);
    }
    @Mock
    UserRepository userRepository;

    @Mock
    CaptchaValidator captchaValidator;

    @InjectMocks
    private UserRegistrationRequestValidator userRegistrationRequestValidator;

    @Test
    public void shouldSuccessfullyValidateUserRegistrationRequest() throws InvalidUserDataException {
        //given

        UserRegisterCore userRegistrationRequest = new UserRegisterCore();
        userRegistrationRequest.setAcceptedRegulations(true);
        userRegistrationRequest.setEmail("ok@ok.pl");
        userRegistrationRequest.setPassword("userPassword_1");
        userRegistrationRequest.setRepeatedPassword("userPassword_1");
        userRegistrationRequest.setCaptcha(VALID_CAPTCHA);


        //when
        userRegistrationRequestValidator.validate(userRegistrationRequest);

        //then
    }

    @Test
    public void shouldSuccessfullyValidateUserRegistrationRequest_1() throws InvalidUserDataException {
        //given
        UserRegisterCore userRegistrationRequest = new UserRegisterCore();
        userRegistrationRequest.setAcceptedRegulations(true);
        userRegistrationRequest.setEmail("real_email@onet.pl");
        userRegistrationRequest.setPassword("realPassword12345");
        userRegistrationRequest.setRepeatedPassword("realPassword12345");
        userRegistrationRequest.setCaptcha(VALID_CAPTCHA);


        //when
        userRegistrationRequestValidator.validate(userRegistrationRequest);

        //then
    }

    @Test
    public void shouldFailOnWrongEmail() {
        //given
        UserRegisterCore userRegistrationRequest = new UserRegisterCore();
        userRegistrationRequest.setAcceptedRegulations(true);
        userRegistrationRequest.setEmail("emailThatIsNotCorrect");
        userRegistrationRequest.setPassword("userPassword_1");
        userRegistrationRequest.setRepeatedPassword("userPassword_1");
        userRegistrationRequest.setCaptcha(VALID_CAPTCHA);


        try {
            //when
            userRegistrationRequestValidator.validate(userRegistrationRequest);
        } catch (RequestFieldsValidationException e) {
            //then
            ErrorResponseMessage messeges = e.getErrorResponseMessage();
            Assert.assertFalse(messeges.isEmpty());
            assertEquals(1, messeges.getMessages().size());
            assertTrue(messeges.getMessages().containsValue(RegistrationErrorMessages.AUTH_ERROR_INVALID_EMAIL.getMessage()));
        }
    }

    @Test
    public void shouldFailOnWrongRegulations() {
        //given
        UserRegisterCore userRegistrationRequest = new UserRegisterCore();
        userRegistrationRequest.setAcceptedRegulations(false);
        userRegistrationRequest.setEmail("ok@ok.pl");
        userRegistrationRequest.setPassword("userPassword_1");
        userRegistrationRequest.setRepeatedPassword("userPassword_1");
        userRegistrationRequest.setCaptcha(VALID_CAPTCHA);


        try {
            //when
            userRegistrationRequestValidator.validate(userRegistrationRequest);
        } catch (RequestFieldsValidationException e) {
            //then
            ErrorResponseMessage messeges = e.getErrorResponseMessage();
            Assert.assertFalse(messeges.isEmpty());
            assertEquals(1, messeges.getMessages().size());
            assertTrue(messeges.getMessages().containsValue(RegistrationErrorMessages.AUTH_ERROR_NOT_ACCEPTED_REGULATIONS.getMessage()));
        }
    }

    @Test
    public void shouldFailOnWrongPassword() {
        //given
        UserRegisterCore userRegistrationRequest = new UserRegisterCore();
        userRegistrationRequest.setAcceptedRegulations(true);
        userRegistrationRequest.setEmail("ok@ok.pl");
        userRegistrationRequest.setPassword("pass");
        userRegistrationRequest.setRepeatedPassword("pass");
        userRegistrationRequest.setCaptcha(VALID_CAPTCHA);


        try {
            //when
            userRegistrationRequestValidator.validate(userRegistrationRequest);
        } catch (RequestFieldsValidationException e) {
            //then
            ErrorResponseMessage messeges = e.getErrorResponseMessage();
            Assert.assertFalse(messeges.isEmpty());
            assertEquals(1, messeges.getMessages().size());
            assertTrue(messeges.getMessages().containsValue(RegistrationErrorMessages.AUTH_ERROR_INVALID_PASSWORD.getMessage()));
        }
    }

    @Test
    public void shouldFailOnWrongRepeatedPassword() {
        //given
        UserRegisterCore userRegistrationRequest = new UserRegisterCore();
        userRegistrationRequest.setAcceptedRegulations(true);
        userRegistrationRequest.setEmail("ok@ok.pl");
        userRegistrationRequest.setPassword("userPassword_1");
        userRegistrationRequest.setRepeatedPassword("userPassword_2");
        userRegistrationRequest.setCaptcha(VALID_CAPTCHA);


        try {
            //when
            userRegistrationRequestValidator.validate(userRegistrationRequest);
        } catch (RequestFieldsValidationException e) {
            //then
            ErrorResponseMessage messeges = e.getErrorResponseMessage();
            Assert.assertFalse(messeges.isEmpty());
            assertEquals(1, messeges.getMessages().size());
            assertTrue(messeges.getMessages().containsValue(RegistrationErrorMessages.AUTH_ERROR_DIFFERENT_PASSWORDS.getMessage()));
        }
    }

    @Test
    public void shouldFailOnExistingUser() {
        Mockito.when(userRepository.existsByEmail(Mockito.anyString())).thenReturn(true);
        //given
        UserRegisterCore userRegistrationRequest = new UserRegisterCore();
        userRegistrationRequest.setAcceptedRegulations(true);
        userRegistrationRequest.setEmail("ok@ok.pl");
        userRegistrationRequest.setPassword("userPassword_1");
        userRegistrationRequest.setRepeatedPassword("userPassword_1");
        userRegistrationRequest.setCaptcha(VALID_CAPTCHA);


        try {
            //when
            userRegistrationRequestValidator.validate(userRegistrationRequest);
        } catch (RequestFieldsValidationException e) {
            //then
            ErrorResponseMessage messeges = e.getErrorResponseMessage();
            Assert.assertFalse(messeges.isEmpty());
            log.info(messeges.getMessages().toString());
            assertEquals(1, messeges.getMessages().size());
            assertTrue(messeges.getMessages().containsValue(RegistrationErrorMessages.AUTH_ERROR_DUPLICATE_EMAIL.getMessage()));
        }
    }

    @Test
    public void shouldFailOnAllFields() {
        Mockito.when(userRepository.existsByEmail(Mockito.anyString())).thenReturn(true);
        //given
        UserRegisterCore userRegistrationRequest = new UserRegisterCore();
        userRegistrationRequest.setAcceptedRegulations(false);
        userRegistrationRequest.setEmail("fakeMail");
        userRegistrationRequest.setPassword("pass1");
        userRegistrationRequest.setRepeatedPassword("pass2");
        userRegistrationRequest.setCaptcha(INVALID_CAPTCHA);

        try {
            //when
            userRegistrationRequestValidator.validate(userRegistrationRequest);
        } catch (RequestFieldsValidationException e) {
            //then
            ErrorResponseMessage messeges = e.getErrorResponseMessage();
            Assert.assertFalse(messeges.isEmpty());
            assertEquals(5,messeges.getMessages().size());
            assertTrue(messeges.getMessages().containsValue(RegistrationErrorMessages.AUTH_ERROR_NOT_ACCEPTED_REGULATIONS.getMessage()));
            assertTrue(messeges.getMessages().containsValue(RegistrationErrorMessages.AUTH_ERROR_DUPLICATE_EMAIL.getMessage()));
            assertTrue(messeges.getMessages().containsValue(RegistrationErrorMessages.AUTH_ERROR_DIFFERENT_PASSWORDS.getMessage()));
        }
    }
}