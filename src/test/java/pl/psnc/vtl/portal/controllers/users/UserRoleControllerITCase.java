package pl.psnc.vtl.portal.controllers.users;

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
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.LogInService;



import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserRoleControllerITCase {

    @Autowired
    private MockMvc mvc;

    private String TOKEN;

    private static final String EMAIL = "email@wp.pl";

    private static final String PASSWORD = "myPassword";

    @Before
    public void init()
            throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);
    }


    @Test
    public void shouldReturnUserRoles()
            throws Exception {
        mvc.perform(get("/api/user-roles").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code", is("contributor")))
                .andExpect(jsonPath("$[1].code", is("verifier")))
                .andExpect(jsonPath("$[2].code", is("admin")));
    }

}
