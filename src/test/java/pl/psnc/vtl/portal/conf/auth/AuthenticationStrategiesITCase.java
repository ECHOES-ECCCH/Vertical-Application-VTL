package pl.psnc.vtl.portal.conf.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.LogInService;
import pl.psnc.vtl.portal.security.LoginCore;

import javax.servlet.http.Cookie;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
@Transactional
public class AuthenticationStrategiesITCase {

    @Autowired
    private MockMvc mvc;

    private String CSRF_TOEKN;
    private String REMEMBER_ME_TOKEN;

    private static final String EMAIL = "email@wp.pl";

    private static final String PASSWORD = "myPassword";

    private static final String USER_INFO_ENCRYPTED = "JKFAyVVhuoZMQCSnpm9ipFGVjR3st8E+0THvUASxcRHJmLYRQ/GpIX9wvr1XDoY/DxEKGh0XAJ0LzykezZIbsjo71WbZzjBa/c0boGzVC8rHrFqh5VSerfSc1a2vl7aTK0xTehGjKrkoYnWdfVkBKAURLANh1OVUw3Ksw9mVhOk=";

    private static final String USER_INFO_CORRUPTED_ENCRYPTED = "QA+zyTsjCKdBySu4Ld6Bxnowq2S4gm1CLKOmvdbXLte33PtXevueLBrRkvmGfR9Rjpb6lk1EuDh1xIafZsqjCFndMCwRGXLp2yIT9XUL7mK/jjV28QORnKvrcA/0cjh5YmILe0g03zLkAxvKHLA7hyVBCYsaprD32Pgt6ufvy2g=";

    private static final String USER_INFO_EMPTY_ENCRYPTED = "eYNdtg5qFI53mfgK9Mpisw+PmiN53ZOK8eNFHlob59W4EPrKHcWDxB/LfuvQ074Zh55TWyagtz4rOG6+/QkHjN6HBcVrw2qJLNu6GSLNGJMZ+yJiOGgxxd+2qDwCY+EO/bWLdK9k99HGGKqFPTs7mtqXs4qb8ikaRPpx7gi9cpw=";

    private final ObjectMapper mapper = new ObjectMapper();


    @Before
    @SneakyThrows
    public void init(){
        REMEMBER_ME_TOKEN = LogInService.getRememberMeToken(mvc, EMAIL, PASSWORD);
        CSRF_TOEKN = LogInService.getCSRFToken(mvc, REMEMBER_ME_TOKEN);
    }
    @Test
    public void shouldObtainJWTTokenWhenSuccessfullySignedIn()
            throws Exception {
        LoginCore loignCore = new LoginCore(EMAIL, PASSWORD);
        mvc.perform(post("/api/auth/sign-in")
                .content(mapper.writeValueAsString(loignCore))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(header().string("Authorization", startsWith("Bearer ")));
    }

    @Test
    public void shouldObtainCookiesRememberMeSuccessfullySignedIn()
            throws Exception {
        LoginCore loignCore = new LoginCore(EMAIL, PASSWORD);
        mvc.perform(post("/api/auth/sign-in")
                .content(mapper.writeValueAsString(loignCore))
                .param("remember-me", "true")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(cookie().exists("remember-me"))
                .andExpect(header().string("Authorization", startsWith("Bearer ")));
    }

    @Test
    public void shouldAuthenticateViaADFS()
            throws Exception {
        mvc.perform(post("/api/auth/fed-sign-in")
                .param("email", USER_INFO_ENCRYPTED)
        )
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(header().string("Authorization", startsWith("Bearer ")));
    }

    @Test(expected = AuthenticationServiceException.class)
    public void shouldThrowExceptionWhenEmailNotPresentInUserInfo()
            throws Exception {
        mvc.perform(post("/api/auth/fed-sign-in")
                .param("email", USER_INFO_CORRUPTED_ENCRYPTED)
        );
    }

    @Test(expected = AuthenticationServiceException.class)
    public void shouldThrowExceptionWhenUserInfoEmpty()
            throws Exception {
        mvc.perform(post("/api/auth/fed-sign-in")
                .param("email", USER_INFO_EMPTY_ENCRYPTED)
        );
    }

    @Test
    public void shouldAccessMethodUsingJWTTokenForADFS()
            throws Exception {
        mvc.perform(get("/api/users/my-profile")
                .header("Authorization", LogInService.getJWTTokenForADFS(mvc, USER_INFO_ENCRYPTED))
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk());
    }

    @Test
    public void shouldAccessMethodUsingJWTToken()
            throws Exception {
        mvc.perform(get("/api/users/my-profile")
                .header("Authorization", LogInService.getJWTToken(mvc, EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk());
    }

    @Test
    public void shouldAccessMethodUsingRememberMeTokenAndAddJWTTokenToResponse()
            throws Exception {
        Cookie rememberMe = new Cookie("remember-me", LogInService.getRememberMeToken(mvc, EMAIL, PASSWORD));
        mvc.perform(get("/api/users/my-profile")
                .cookie(rememberMe)
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(header().string("Authorization", startsWith("Bearer ")))
                .andExpect(jsonPath("user.email", is(EMAIL)));
        ;
    }

    @Test
    public void shouldAccessMethodBeingSuperAdmin()
            throws Exception {

        String superAdminPassword = "superadmin";
        String superAdminEmail = "superadmin@wp.pl";
        mvc.perform(put("/api/admin/users/{id}?enabled=true", 1)
                .header("Authorization", LogInService.getJWTToken(mvc, superAdminEmail, superAdminPassword))
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk());
    }

    @Test
    public void shouldFailAccessMethodBeingUser()
            throws Exception {

        String emailToBeDisabled = "email2@wp.pl";
        mvc.perform(put("/api/admin/users/disable/{email}", emailToBeDisabled)
                .header("Authorization", LogInService.getJWTToken(mvc, EMAIL, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isForbidden());
    }


    @Test
    public void shouldDeactivateRememberMeTokenWhenSignedOut()
            throws Exception {
        Cookie rememberMe = new Cookie("remember-me", LogInService.getRememberMeToken(mvc, EMAIL, PASSWORD));
        mvc.perform(get("/api/auth/sign-out")
                .cookie(rememberMe))

                .andExpect(status().isOk())
                .andExpect(cookie().value("remember-me", isEmptyOrNullString()))
                .andExpect(cookie().maxAge("remember-me", 0));
    }

    @Test
    public void shouldAccessDeleteMethodAuthenticatingUsingRememberMeTokenAndCSRFToken()
            throws Exception {
        Cookie rememberMe = new Cookie("remember-me", REMEMBER_ME_TOKEN);
        Integer id = 2;

        mvc.perform(delete("/api/verses/{verseId}", id)
                .cookie(rememberMe)
                .header("X-CSRF-TOKEN", CSRF_TOEKN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    public void shouldNotAccessDeleteMethodAuthenticatingUsingOnlyRememberMeToken()
            throws Exception {
        Cookie rememberMe = new Cookie("remember-me", REMEMBER_ME_TOKEN);
        Integer id = 2;

        mvc.perform(delete("/api/verses/{verseId}", id)
                .cookie(rememberMe)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

    }

}
