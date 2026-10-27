package pl.psnc.vtl.portal.controllers.iiifserver;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
@Transactional
public class IIIFServerControllerITCase {

    @Autowired
    private MockMvc mvc;

    private static final String EMAIL_0 = "email@wp.pl";

    private static final String EMAIL_1 = "userWithoutPermissionToTranscription@wp.pl";

    private static final String PASSWORD = "myPassword";


    @Test
    public void shouldReturnResponseFromIIIFServer()
            throws Exception {
        long pageId = 1;

        mvc.perform(get("/api/iiif/pages/{pageId}/info.json", pageId)
                .header("Authorization", LogInService.getJWTToken(mvc, EMAIL_0, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.width", Matchers.is(2596))).andExpect(jsonPath("$.height", Matchers.is(3760)));

    }
    
    @Test
    public void shouldThrowIIIFServerRepositoryException()
            throws Exception {
        long pageId = 999;

        mvc.perform(get("api/iiif/pages/{pageId}/info.json", pageId)
                .header("Authorization", LogInService.getJWTToken(mvc, EMAIL_0, PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }
}
