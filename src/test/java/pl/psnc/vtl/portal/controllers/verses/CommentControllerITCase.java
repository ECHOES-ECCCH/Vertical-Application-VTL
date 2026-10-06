package pl.psnc.vtl.portal.controllers.verses;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
import pl.psnc.vtl.portal.LogInService;
import pl.psnc.vtl.portal.dto.verses.CommentCore;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class CommentControllerITCase {

    @Autowired
    private MockMvc mvc;

    ObjectMapper objectMapper = new ObjectMapper();

    private static final String EMAIL = "email@wp.pl";

    private static final String PASSWORD = "myPassword";

    private String TOKEN;

    private String CONTRIBUTOR_TOKEN;

    private String CREATOR_OF_TRANSCRIPTION_TOKEN;

    @Before
    public void init()
            throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);
        CONTRIBUTOR_TOKEN = LogInService.getJWTToken(mvc, "transcriber@wp.pl", PASSWORD);
        CREATOR_OF_TRANSCRIPTION_TOKEN = LogInService.getJWTToken(mvc, "email2@wp.pl", PASSWORD);

    }

    @Test
    public void shouldAddComment()
            throws Exception {

        CommentCore comment = new CommentCore();
        String content = "Nowy Komentarz";
        comment.setContent(content);

        Integer verseId = 1;

        mvc.perform(post("/api/comments/{verseId}", verseId)
                .header("Authorization", TOKEN)
                .content(objectMapper.writeValueAsString(comment))
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(6)));
    }

    @Test
    public void shouldNotAddCommentWhenUserIsContributorAndVerseIsVerified()
            throws Exception {

        CommentCore comment = new CommentCore();
        comment.setContent("Nowy Komentarz");

        mvc.perform(post("/api/comments/{verseId}", 72)
                .header("Authorization", CONTRIBUTOR_TOKEN)
                .content(objectMapper.writeValueAsString(comment))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldAddCommentWhenUserIsAdminAndVerseIsVerified()
            throws Exception {

        CommentCore comment = new CommentCore();
        comment.setContent("Nowy Komentarz");

        mvc.perform(post("/api/comments/{verseId}", 72)
                .header("Authorization", CREATOR_OF_TRANSCRIPTION_TOKEN)
                .content(objectMapper.writeValueAsString(comment))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(1)));
    }


    @Test
    public void shouldReturnVerseComments()
            throws Exception {

        mvc.perform(get("/api/comments/{verseId}", 2)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }



    @Test
    public void shouldDeleteComment()
            throws Exception {
        int commentId = 7;

        mvc.perform(get("/api/comments/{verseId}", 3)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mvc.perform(delete("/api/comments/{commentId}", commentId)
                .header("Authorization", LogInService.getJWTToken(mvc, "email2@wp.pl", "myPassword"))
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk());

        mvc.perform(get("/api/comments/{verseId}", 3)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }



    @Test
    public void shouldNotDeleteCommentWhenUserIsNotCreator()
            throws Exception {
        int commentId = 6;

        mvc.perform(get("/api/comments/{verseId}", 2)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())

                .andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(delete("/api/comments/{commentId}", commentId)
                .header("Authorization", LogInService.getJWTToken(mvc, "email1@wp.pl", "myPassword"))
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/comments/{verseId}", 2)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

    }

}
