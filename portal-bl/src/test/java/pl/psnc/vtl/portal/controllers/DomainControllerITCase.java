package pl.psnc.vtl.portal.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import pl.psnc.vtl.portal.LogInService;
import pl.psnc.vtl.portal.model.imports.FileType;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class DomainControllerITCase {

    private static final String EMAIL = "email@wp.pl";
    private static final String PASSWORD = "myPassword";
    @Autowired
    private MockMvc mvc;
    private String TOKEN;


    @Before
    public void init()
            throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);
    }


    @Test
    public void shouldReturnAllDomains()
            throws Exception {

        mvc.perform(get("/api/domains").header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(12)));
    }


    @Test
    public void shouldReturnIIIFDomains()
            throws Exception {

        mvc.perform(get("/api/domains").param("file-type", FileType.IIIF.getValue()).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)));
    }


    @Test
    public void shouldReturnIIIFDomainsWhichContainsSubstring()
            throws Exception {

        mvc.perform(get("/api/domains").param("file-type", FileType.IIIF.getValue()).param("substring", "diGiTaLe")
                .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0]", is("digitale-sammlungen.de")));
    }


    @Test
    public void shouldReturnDomainsWhichContainsSubstring()
            throws Exception {

        mvc.perform(get("/api/domains").param("substring", "diGiTaL").header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$[2]", is("digital.bodleian.ox.ac.uk")))
                .andExpect(jsonPath("$", hasSize(3)));
    }
}
