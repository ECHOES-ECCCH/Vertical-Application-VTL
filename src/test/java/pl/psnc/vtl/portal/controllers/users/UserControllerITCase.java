package pl.psnc.vtl.portal.controllers.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.LogInService;
import pl.psnc.vtl.portal.dto.users.UserPasswordCore;



import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Transactional
public class UserControllerITCase {

    @Autowired
    private MockMvc mvc;

    private String TOKEN;

    private static final String EMAIL = "email@wp.pl";

    private static final String PASSWORD = "myPassword";

    ObjectMapper objectMapper = new ObjectMapper();


    @Before
    public void init()
            throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);
    }


    @Test
    public void shouldReturnUserByEmail()
            throws Exception {
        String email = "email@wp.pl";

        mvc.perform(
                get("/api/users/{email}", email).header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("email", is(email)))
                .andExpect(jsonPath("enabled", is(true)));
    }


    @Test
    public void shouldNotReturnUserWhenNotExist()
            throws Exception {
        String nonExistingEmail = "myNonExistingEmail@wp.pl";

        mvc.perform(get("/api/users/{email}", nonExistingEmail).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }


    @Test
    public void shouldReturnMyProfile()
            throws Exception {

        mvc.perform(get("/api/users/my-profile").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk()).andExpect(jsonPath("user.email", is(EMAIL)));
    }


    @Test
    public void shouldDisableUserWhenExists()
            throws Exception {
        long userId = 5;
        String superAdminPassword = "superadmin";
        String superAdminEmail = "superadmin@wp.pl";

        mvc.perform(put("/api/admin/users/{userId}?enabled=false", userId)
                .header("Authorization", LogInService.getJWTToken(mvc, superAdminEmail, superAdminPassword))
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk()).andExpect(jsonPath("enabled", is(false)));
    }


    @Test
    public void shouldNotDisableUserWhenNotExists()
            throws Exception {
        String nonExistingUserName = "myNonExistingUserNick";
        String superAdminPassword = "superadmin";
        String superAdminEmail = "superadmin@wp.pl";

        mvc.perform(put("/api/admin/users/disable/{username}", nonExistingUserName)
                .header("Authorization", LogInService.getJWTToken(mvc, superAdminEmail, superAdminPassword))
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isNotFound());
    }


    @Test
    public void shouldChangePassword()
            throws Exception {
        String newPassword = "K@sia123";

        UserPasswordCore password = new UserPasswordCore();
        password.setCurrentPassword(PASSWORD);
        password.setNewPassword(newPassword);
        password.setRepeatedNewPassword(newPassword);
        String token = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);

        mvc.perform(put("/api/users/my-password").header("Authorization", TOKEN)
                .content(objectMapper.writeValueAsString(password)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    public void shouldNotChangePasswordWhenRepeatedPasswordIsWrong()
            throws Exception {

        String wrongRepeatedPassword = "wrongPassword";
        String newPassword = "K@sia123";

        UserPasswordCore password = new UserPasswordCore();
        password.setCurrentPassword(PASSWORD);
        password.setNewPassword(newPassword);
        password.setRepeatedNewPassword(wrongRepeatedPassword);

        mvc.perform(put("/api/users/my-password").header("Authorization", TOKEN)
                .content(objectMapper.writeValueAsString(password)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("repeatedNewPassword", is("auth.error.differentPasswords")));
    }

}
