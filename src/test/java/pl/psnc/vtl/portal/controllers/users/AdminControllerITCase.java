package pl.psnc.vtl.portal.controllers.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import pl.psnc.vtl.portal.LogInService;
import pl.psnc.vtl.portal.dto.users.UserId;
import pl.psnc.vtl.portal.model.users.SecurityGroup;
import pl.psnc.vtl.portal.model.users.User;
import pl.psnc.vtl.portal.repositories.users.UserRepository;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminControllerITCase {

    @Autowired
    private MockMvc mvc;

    private String TOKEN;

    private String SUPER_ADMIN_TOKEN;

    private static final String EMAIL = "email@wp.pl";

    private static final String PASSWORD = "myPassword";

    private static final String SUPER_ADMIN_EMAIL = "superadmin@wp.pl";

    private static final String SUPER_ADMIN_PASSWORD = "superadmin";

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;


    @Before
    public void init()
            throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);
        SUPER_ADMIN_TOKEN = LogInService.getJWTToken(mvc, SUPER_ADMIN_EMAIL, SUPER_ADMIN_PASSWORD);
    }


    @Test
    public void shouldReturnUsersForSuperAdmin()
            throws Exception {

        UserId userId = new UserId();
        userId.setId(9L);

        mvc.perform(get("/api/admin/users/").header("Authorization", SUPER_ADMIN_TOKEN)
                .content(objectMapper.writeValueAsString(userId)).param("page", "1").param("perPage", "5"))
                .andExpect(status().isOk()).andExpect(jsonPath("users", hasSize(5)));

        mvc.perform(get("/api/admin/users/").header("Authorization", SUPER_ADMIN_TOKEN)
                .content(objectMapper.writeValueAsString(userId)).param("page", "2").param("perPage", "3"))
                .andExpect(status().isOk()).andExpect(jsonPath("users", hasSize(3)));

        mvc.perform(get("/api/admin/users/").header("Authorization", SUPER_ADMIN_TOKEN)
                .content(objectMapper.writeValueAsString(userId)).param("page", "1").param("perPage", "100"))
                .andExpect(status().isOk()).andExpect(jsonPath("users", hasSize(16)));

    }


    @Test
    public void shouldReturnFoundListOfUsers()
            throws Exception {
        mvc.perform(get("/api/admin/users/").header("Authorization", SUPER_ADMIN_TOKEN).param("page", "1")
                .param("perPage", "5").param("q", "SuperAdmin"))

                .andExpect(status().isOk()).andExpect(jsonPath("users", hasSize(2)));

    }


    @Test
    public void shouldReturnEmptyListWhenNoMatchingResultFound()
            throws Exception {
        mvc.perform(get("/api/admin/users/").header("Authorization", SUPER_ADMIN_TOKEN).param("page", "1")
                .param("perPage", "5").param("q", "should not find anything"))

                .andExpect(status().isOk()).andExpect(jsonPath("users", hasSize(0)));

    }


    @Test
    public void shouldReturnListWhenSQLSensitiveCharacterGiven()
            throws Exception {
        mvc.perform(get("/api/admin/users/").header("Authorization", SUPER_ADMIN_TOKEN).param("page", "1")
                .param("perPage", "5").param("q", "'"))

                .andExpect(status().isOk()).andExpect(jsonPath("users", hasSize(0)));

    }


    @Test
    public void shouldReturnForbiddenForNotSuperAdmin()
            throws Exception {

        UserId userId = new UserId();
        userId.setId(1L);

        mvc.perform(
                get("/api/admin/users").header("Authorization", TOKEN).content(objectMapper.writeValueAsString(userId))
                        .param("page", "1").param("perPage", "5")).andExpect(status().isForbidden());

    }


    @Test
    public void shouldUpdateUserRole()
            throws Exception {

        UserId userId = new UserId();
        userId.setId(12L);
        mvc.perform(put("/api/admin/users/{userId}/role", userId.getId()).header("Authorization", SUPER_ADMIN_TOKEN)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(userId))
                .param("role", "super_admin")).andExpect(status().isOk());
        User updatedUser = userRepository.findById(userId.getId()).orElseThrow();
        assertThat(updatedUser.getSecurityGroup(), is(SecurityGroup.SUPER_ADMIN));
    }


    @Test
    public void shouldReturnNotFoundWhenRoleNotExists()
            throws Exception {

        mvc.perform(put("api/admin/users/{userId}/role", 12).header("Authorization", SUPER_ADMIN_TOKEN)
                .contentType(MediaType.APPLICATION_JSON).param("role", "not_existing_role")).andExpect(status().isNotFound());

    }


    @Test
    public void shouldReturnForbiddenForNotSuperAdmin_1()
            throws Exception {

        UserId userId = new UserId();
        userId.setId(12L);

        mvc.perform(put("/api/admin/users/role").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userId)).param("role", "SUPER_ADMIN"))
                .andExpect(status().isForbidden());

    }


    @Test
    public void shouldReturnEntityNotFoundExceptionWhenUserNotExists()
            throws Exception {

        mvc.perform(put("/api/admin/users/{userId}/role", -1).header("Authorization", SUPER_ADMIN_TOKEN)
                .contentType(MediaType.APPLICATION_JSON).param("role", "SUPER_ADMIN")).andExpect(status().isNotFound());

    }


    @Test
    public void shouldUpdateUserQuota()
            throws Exception {
        int quotaInMB = 5000;
        long quotaInB = 5000 * 1000000L;

        mvc.perform(put("/api/admin/users/{userId}/quota", 1).header("Authorization", SUPER_ADMIN_TOKEN)
                .contentType(MediaType.APPLICATION_JSON).param("quota", String.valueOf(quotaInMB))).andExpect(status().isOk());
        assertEquals(quotaInB, (long) userRepository.findById(1L).orElseThrow().getQuota());
    }

    @Test
    public void shouldThrowQuotaException()
            throws Exception {
        mvc.perform(put("/api/admin/users/{userId}/quota", 1).header("Authorization", SUPER_ADMIN_TOKEN)
                .contentType(MediaType.APPLICATION_JSON).param("quota", "-1")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("code", is("quota.error.invalid.parameter")));
    }


    @Test
    public void shouldMarkUserAsTrustedContributor() throws Exception {
        mvc.perform(
                put("/api/admin/users/{userId}/trust", 8)
                        .header("Authorization", SUPER_ADMIN_TOKEN)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(8)))
                .andExpect(jsonPath("email", is("myUsernameWithOneTranscription@wp.pl")))
                .andExpect(jsonPath("enabled", is(true)))
                .andExpect(jsonPath("securityGroup", is("USER")))
                .andExpect(jsonPath("trustedContributor", is(true)));

        mvc.perform(
                get("/api/users/{email}", "myUsernameWithOneTranscription@wp.pl")
                        .header("Authorization", TOKEN)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(8)))
                .andExpect(jsonPath("email", is("myUsernameWithOneTranscription@wp.pl")))
                .andExpect(jsonPath("enabled", is(true)))
                .andExpect(jsonPath("securityGroup", is("USER")))
                .andExpect(jsonPath("trustedContributor", is(true)));
    }

    @Test
    public void shouldMarkUserAsUntrustedContributor() throws Exception {
        mvc.perform(
                put("/api/admin/users/{userId}/trust", 9)
                        .param("trust", "false")
                        .header("Authorization", SUPER_ADMIN_TOKEN)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(9)))
                .andExpect(jsonPath("email", is("superadmin@wp.pl")))
                .andExpect(jsonPath("enabled", is(true)))
                .andExpect(jsonPath("securityGroup", is("SUPER_ADMIN")))
                .andExpect(jsonPath("trustedContributor", is(false)));

        mvc.perform(
                get("/api/users/{email}", "superadmin@wp.pl")
                        .header("Authorization", TOKEN)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(9)))
                .andExpect(jsonPath("email", is("superadmin@wp.pl")))
                .andExpect(jsonPath("enabled", is(true)))
                .andExpect(jsonPath("securityGroup", is("SUPER_ADMIN")))
                .andExpect(jsonPath("trustedContributor", is(false)));
    }
}
