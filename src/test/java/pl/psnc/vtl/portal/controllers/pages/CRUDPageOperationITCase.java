package pl.psnc.vtl.portal.controllers.pages;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.LogInService;
import pl.psnc.vtl.portal.repositories.pages.PageRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CRUDPageOperationITCase {

    private static final String EMAIL = "email@wp.pl";
    private static final String PASSWORD = "myPassword";

    @Autowired
    private MockMvc mvc;
    private String TOKEN;

    @Autowired
    private PageRepository pageRepository;

    @Before
    public void init() throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);
    }

    @Ignore("Test passes only once. ")
    @Test
    public void shouldDeletePage() throws Exception {

        Long id = 105L;
        mvc.perform(delete("/api/pages/{pageId}", id)
                .header("Authorization", TOKEN))

                .andExpect(MockMvcResultMatchers.status().isOk());

        Assert.assertFalse(pageRepository.existsById(id));
    }
}
