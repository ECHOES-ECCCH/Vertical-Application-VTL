package pl.psnc.vtl.portal.controllers.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.dto.registration.UserRegisterCore;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.model.users.SecurityGroup;
import pl.psnc.vtl.portal.services.registration.CaptchaValidator;
import pl.psnc.vtl.portal.services.registration.RegistrationErrorMessages;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Transactional
public class RegistrationControllerITCase {

    @Autowired
    private MockMvc mvc;

    private final String VALID_CAPTCHA = "valid";
    @MockBean
    CaptchaValidator captchaValidator;

    ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void init(){
        doReturn(true).when(captchaValidator).isValidCaptcha(VALID_CAPTCHA);
    }

    @Test
    public void shouldCreateNewUser()
            throws Exception {

        UserRegisterCore newUser = new UserRegisterCore();
        String email = "newemail@wp.pl";
        String password = "djBf5sff";
        String repeatedPassword = "djBf5sff";
        String clientRedirectUrl = "httpRedirect";
        StaticFileLanguage lang =  StaticFileLanguage.EN;

        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setRepeatedPassword(repeatedPassword);
        newUser.setAcceptedRegulations(true);
        newUser.setCaptcha(VALID_CAPTCHA);
        newUser.setClientRedirectURL(clientRedirectUrl);
        mvc.perform(post("/api/auth/sign-up")
                .content(objectMapper.writeValueAsString(newUser))
                .header("language", lang)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled",  is(false)))
                .andExpect(jsonPath("$.email",  is(email)))
                .andExpect(jsonPath("$.securityGroup",  is((SecurityGroup.USER.name()))))
                .andExpect(jsonPath("$.quota",  notNullValue()))
                .andExpect(jsonPath("$.usedSpaceQuota", is(0)));

    }


    @Test
    public void shouldNotCreateUserWhenPasswordsAreNotTeSame()
            throws Exception {

        UserRegisterCore newUser = new UserRegisterCore();


        String email = "newemail2@wp.pl";
        String password = "djBf5sff";
        String repeatedPassword = "djBf5sf5";
        String clientRedirectUrl = "httpRedirect";
        StaticFileLanguage lang =  StaticFileLanguage.EN;



        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setRepeatedPassword(repeatedPassword);
        newUser.setAcceptedRegulations(true);
        newUser.setCaptcha(VALID_CAPTCHA);
        newUser.setClientRedirectURL(clientRedirectUrl);

        mvc.perform(post("/api/auth/sign-up")
                .header("language", lang)
                .content(objectMapper.writeValueAsString(newUser)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.repeatedPassword", is(RegistrationErrorMessages.AUTH_ERROR_DIFFERENT_PASSWORDS.getMessage())));
    }


    @Test
    public void shouldNotCreateUserWhenUsernameExists()
            throws Exception {
        UserRegisterCore newUser = new UserRegisterCore();

        String username = "myExistingUserInDatabase";
        String email = "email@wp.pl";
        String password = "djBf5sff";
        String repeatedPassword = "djBf5sff";
        String clientRedirectUrl = "httpRedirect";
        StaticFileLanguage lang =  StaticFileLanguage.EN;


        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setRepeatedPassword(repeatedPassword);
        newUser.setAcceptedRegulations(true);
        newUser.setCaptcha(VALID_CAPTCHA);
        newUser.setClientRedirectURL(clientRedirectUrl);


        mvc.perform(post("/api/auth/sign-up", newUser)
                .header("language", lang)
                .content(objectMapper.writeValueAsString(newUser)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email", is(RegistrationErrorMessages.AUTH_ERROR_DUPLICATE_EMAIL.getMessage())));
    }


    @Test
    public void shouldNotCreateUserWhenPasswordsDoNotMatchRequirements()
            throws Exception {
        UserRegisterCore newUser = new UserRegisterCore();

        String username = "newU";
        String email = "newEmail";
        String password = "djBfgsff";
        String repeatedPassword = "djBfgsff";
        String clientRedirectUrl = "httpRedirect";
        StaticFileLanguage lang =  StaticFileLanguage.EN;

        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setRepeatedPassword(repeatedPassword);
        newUser.setAcceptedRegulations(true);
        newUser.setCaptcha(VALID_CAPTCHA);
        newUser.setClientRedirectURL(clientRedirectUrl);

        mvc.perform(post("/api/auth/sign-up")
                .header("language", lang)
                .content(objectMapper.writeValueAsString(newUser))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password", is(RegistrationErrorMessages.AUTH_ERROR_INVALID_PASSWORD.getMessage())));
    }

    @Test
    public void shouldNotCreateUserWhenUserDoNotAcceptedRegulations()
            throws Exception {

        UserRegisterCore newUser = new UserRegisterCore();

        String username = "newUsernam555e";
        String email = "newEmail@wp.pl";
        String password = "djBf5sff";
        String repeatedPassword = "djBf5sff";
        String clientRedirectUrl = "httpRedirect";
        StaticFileLanguage lang =  StaticFileLanguage.EN;
        String captcha = "captchaString";


        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setRepeatedPassword(repeatedPassword);
        newUser.setAcceptedRegulations(false);
        newUser.setCaptcha(captcha);
        newUser.setClientRedirectURL(clientRedirectUrl);

        mvc.perform(post("/api/auth/sign-up")
                .header("language", lang)
                .content(objectMapper.writeValueAsString(newUser))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.acceptedRegulations", is(RegistrationErrorMessages.AUTH_ERROR_NOT_ACCEPTED_REGULATIONS.getMessage())));
    }


}
