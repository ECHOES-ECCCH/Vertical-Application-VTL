package pl.psnc.vtl.portal.controllers.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.LogInService;
import pl.psnc.vtl.portal.services.registration.CaptchaValidator;
import pl.psnc.vtl.portal.services.users.UsersCommunicationMail;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserCommunicationControllerITCase {

    @Autowired
    private MockMvc mvc;

    private String TOKEN;

    private static final String EMAIL = "email@wp.pl";

    private static final String PASSWORD = "myPassword";

    ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    CaptchaValidator captchaValidator;

    @Before
    public void init()
            throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);
    }

    /**
     * to avoid time wasting sending request through postman-ish apps
     * Ignored to avoid sending unnecessary emails
     * @throws Exception
     */
    @Ignore
    @Test
    public void shouldSendEmail()
            throws Exception {
        //given
        Long receiverId = 16L; //one that has email vtl.portal.dev@gmail.com
        String subject = "test in vlt bl portal " + Instant.now() ;
        String message = "this is test from " + Instant.now();
        String captcha = "captcha";
        UsersCommunicationMail dto = new UsersCommunicationMail(receiverId, subject, message, captcha);
        //and
        Mockito.doReturn(true).when(captchaValidator).isValidCaptcha(captcha);
        //when
        mvc.perform(post("/api/communication/email")
                .content(objectMapper.writeValueAsString(dto))
                .header("Authorization", TOKEN).header("Language","pl").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());
    }
}