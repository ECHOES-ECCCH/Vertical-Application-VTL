package pl.psnc.vtl.portal.security.csrf;

import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import pl.psnc.vtl.portal.LogInService;

import javax.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CSRFControllerITCase {


    private String CSRF_TOEKN;
    private String REMEMBER_ME_TOKEN;

    private static final String EMAIL = "email@wp.pl";

    private static final String PASSWORD = "myPassword";

    @Autowired
    private MockMvc mvc;

    @Before
    @SneakyThrows
    public void init(){
        REMEMBER_ME_TOKEN = LogInService.getRememberMeToken(mvc, EMAIL, PASSWORD);
        CSRF_TOEKN = LogInService.getCSRFToken(mvc, REMEMBER_ME_TOKEN);
    }

    @Test
    public void shouldReturnCSRFTokenInHeader() throws Exception {
        mvc.perform(get("/api/auth/csrf/token")
        .cookie(new Cookie("remember-me", REMEMBER_ME_TOKEN)))
                .andExpect(header().exists("CSRF-TOKEN"))
                .andExpect(header().stringValues("CSRF-TOKEN", CSRF_TOEKN));
    }
}