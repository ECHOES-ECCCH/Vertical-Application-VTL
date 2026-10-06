package pl.psnc.vtl.portal;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.psnc.vtl.portal.security.LoginCore;

import javax.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class LogInService {

    private static ObjectMapper mapper = new ObjectMapper();

    public static String getJWTToken(MockMvc mvc, String email, String password)
            throws Exception {
        LoginCore loginCore = new LoginCore(email, password);
        return mvc.perform(
                post("/api/auth/sign-in")
                        .content(mapper.writeValueAsString(loginCore))
                        .contentType(MediaType.APPLICATION_JSON))
                        .andReturn().getResponse().getHeader("Authorization");
    }

    public static String getJWTTokenForADFS(MockMvc mvc, String email)
            throws Exception {
        return mvc.perform(
                post("/api/auth/fed-sign-in").param("email", email))
                .andReturn().getResponse().getHeader("Authorization");
    }

    public static String getRememberMeToken(MockMvc mvc, String email, String password)
            throws Exception {
        LoginCore loginCore = new LoginCore(email, password);
        return mvc.perform(
                post("/api/auth/sign-in")
                        .param("remember-me", "true")
                        .content(mapper.writeValueAsString(loginCore))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getCookie("remember-me").getValue();
    }

    @SneakyThrows
    public static String getCSRFToken(MockMvc mvc, String rememberMeToken){
        return mvc.perform(
                get("/api/auth/csrf/token")
                        .cookie(new Cookie("remember-me", rememberMeToken)))
                .andReturn()
                .getResponse()
                .getHeader("CSRF-TOKEN");
    }
}
