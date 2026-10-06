package pl.psnc.vtl.portal.controllers.verses;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.MatcherAssert;
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
import org.springframework.test.web.servlet.MvcResult;
import pl.psnc.vtl.portal.LogInService;
import pl.psnc.vtl.portal.model.verses.FormeWork;
import pl.psnc.vtl.portal.model.verses.Heading;
import pl.psnc.vtl.portal.model.verses.ListForm;

import javax.transaction.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
@Transactional
public class VerseContentControllerITCase {

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
    public void shouldChangeVerseEndOfParagraph()
            throws Exception {

        int verseId = 1;

        mvc.perform(put("/api/verses/{verseId}/formatting/end-of-paragraph", verseId).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("endOfParagraph", is(true)));
    }


    @Test
    public void shouldChangeVerseEndOfListElement()
            throws Exception {
        int verseId = 1;
        ListForm listForm = ListForm.END_OF_ITEM;

        mvc.perform(put("/api/verses/{verseId}/formatting/list-form/{listForm}", verseId, listForm).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("listForm", is("end-of-item")));
    }


    @Test
    public void shouldChangeVerseHeading()
            throws Exception {
        int verseId = 1;
        Heading heading = Heading.END_OF_HEADING_1;

        mvc.perform(put("/api/verses/{verseId}/formatting/heading/{heading}", verseId, heading)
                .header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("heading", is("end-of-heading-1")));
    }


    @Test
    public void shouldChangeVerseFormeWork()
            throws Exception {
        int verseId = 1;
        FormeWork formeWork = FormeWork.FOOTER;

        mvc.perform(put("/api/verses/{verseId}/formatting/forme-work/{formeWork}", verseId, formeWork)
                .header("Authorization", TOKEN)).andExpect(status().isOk())
                .andExpect(jsonPath("formeWork", is("footer")));
    }


    @Test
    public void shouldClearVerseFormatting()
            throws Exception {
        int verseId = 1;

        mvc.perform(put("/api/verses/{verseId}/formatting/clear", verseId).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("formeWork", nullValue()))
                .andExpect(jsonPath("heading", nullValue()))
                .andExpect(jsonPath("listForm", nullValue()))
                .andExpect(jsonPath("tabularForm", nullValue()))
                .andExpect(jsonPath("endOfParagraph", is(false)))
                .andExpect(jsonPath("bold", is(false)))
                .andExpect(jsonPath("italic", is(false)))
                .andExpect(jsonPath("strikethrough", is(false)))
                .andExpect(jsonPath("underline", is(false)))
                .andExpect(jsonPath("annotation", is(false)))
                .andExpect(jsonPath("marginalia", is(false)))
                .andExpect(jsonPath("notatedMusic", is(false)))
                .andExpect(jsonPath("imageCaption", is(false)));
    }


    @Test
    public void shouldRecognizeSubimage()
            throws Exception {
        //given
        Long verseId = 1L;
        String verseContent = "KOMITET";

        //when
        MvcResult mvcResult = mvc.perform(
                put("/api/verses/{verseId}/content-recognition", verseId).header("Authorization", TOKEN)
                        .param("lang", "pol").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();

        //then
        MatcherAssert.assertThat(contentAsString, containsString(verseContent));
    }


    @Test
    public void shouldNotRecognizeSubimageWhenHasNoPermission()
            throws Exception {
        //given
        Long verseId = 1L;

        String token = LogInService.getJWTToken(mvc, "myExistingUserInDatabase@wp.pl", PASSWORD);

        //when
        mvc.perform(put("/api/verses/{verseId}/content-recognition", verseId).header("Authorization", token)
                .param("lang", "pol").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }


    @Test
    public void shouldNotRecognizeSubimageAndReturn404WhenThereIsNoVerse()
            throws Exception {
        //given
        Long verseId = 999L;

        String token = LogInService.getJWTToken(mvc, "myExistingUserInDatabase@wp.pl", PASSWORD);

        //when
        mvc.perform(put("/api/verses/{verseId}/content-recognition", verseId).header("Authorization", token)
                .param("lang", "pol").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
    }


    @Test
    public void shouldUpdateVerseContent() throws Exception {
        Long verseId = 1L;
        Long pageId = 1L;
        Long transcriptionId = 1L;
        String newVerseContent = "New verse content to test.";

        mvc.perform(put("/api/verses/{verseId}/modify-content", verseId)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(newVerseContent));

        mvc.perform(get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, pageId)
                        .header("Authorization", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("verses[0].content", is(newVerseContent)));

    }

    @Test
    public void shouldChangeVerseEndOfAnnotation()
            throws Exception {

        int verseId = 1;

        mvc.perform(put("/api/verses/{verseId}/formatting/annotation", verseId).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("annotation", is(true)));
    }

    @Test
    public void shouldChangeVerseEndOfMarginalia()
            throws Exception {

        int verseId = 1;

        mvc.perform(put("/api/verses/{verseId}/formatting/marginalia", verseId).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("marginalia", is(true)));
    }

    @Test
    public void shouldChangeVerseEndOfBold()
            throws Exception {

        int verseId = 1;

        mvc.perform(put("/api/verses/{verseId}/formatting/font/bold", verseId).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("bold", is(true)));
    }

    @Test
    public void shouldChangeVerseEndOfItalic()
            throws Exception {

        int verseId = 1;

        mvc.perform(put("/api/verses/{verseId}/formatting/font/italic", verseId).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("italic", is(true)));
    }

    @Test
    public void shouldChangeVerseEndOfUnderline()
            throws Exception {

        int verseId = 1;

        mvc.perform(put("/api/verses/{verseId}/formatting/font/underline", verseId).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("underline", is(true)));
    }

    @Test
    public void shouldChangeVerseEndOfStrikethrough()
            throws Exception {

        int verseId = 1;

        mvc.perform(put("/api/verses/{verseId}/formatting/font/strikethrough", verseId).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("strikethrough", is(true)));
    }

    @Test
    public void shouldChangeVerseNotatedMusic()
            throws Exception {

        int verseId = 1;

        mvc.perform(put("/api/verses/{verseId}/formatting/notated-music", verseId).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("notatedMusic", is(true)));
    }

}
