package pl.psnc.vtl.portal.controllers.iiifsearch;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import pl.psnc.vtl.portal.LogInService;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.profiles.active:test")
public class IIIFSearchControllerITCase {

    @Autowired
    private MockMvc mvc;

    private String TOKEN;

    private static final String EMAIL = "email@wp.pl";

    private static final String PASSWORD = "myPassword";

    @Before
    public void init() throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);
    }

    @Test
    public void shouldPerformIIIFSearchOnlyForSpecifiedLayer() throws Exception {
        final long transcriptionId = 45;
        final long layerId = 3;
        final String q = "warstwie";

        mvc.perform(get("/api/iiif/transcriptions/{transcriptionId}/search", transcriptionId)
                        .header("Authorization", TOKEN)
                        .param("q", q)
                        .param("layerId", String.valueOf(layerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("resources", hasSize(1)))
                .andExpect(jsonPath("hits", hasSize(1)));
    }
}
