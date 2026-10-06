package pl.psnc.vtl.portal.controllers.transcriptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
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
import pl.psnc.vtl.portal.services.verses.TranscriptionVerseUserStatistic;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
public class TranscriptionVerseStatisticsControllerITCase {


    @Autowired
    private MockMvc mvc;

    private String TOKEN;

    private String TOKEN_FOR_NON_VERIFIER;

    private static final String EMAIL = "email3@wp.pl";

    private static final String NON_VERIFIER_EMAIL = "email6@wp.pl";

    private static final String PASSWORD = "myPassword";

    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void init()
            throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);
        TOKEN_FOR_NON_VERIFIER =  LogInService.getJWTToken(mvc, NON_VERIFIER_EMAIL, PASSWORD);
    }

    @Test
    public void shouldReturnTranscriptionStatistics()
            throws Exception {
        long transcriptionId = 1L;
        mvc.perform(put("/api/verses/{verseId}/modify-content", 1)
                .header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("newContentnjknkjnjkknkjnknknjk"));
        mvc.perform(
                get("/api/transcriptions/{transcriptionId}/verse-statistics", transcriptionId)
                        .header("Authorization", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(3)))
                .andExpect(jsonPath("$[0].email", Matchers.isIn(emails)))
                .andExpect(jsonPath("$[1].email", Matchers.isIn(emails)))
                .andExpect(jsonPath("$[2].email", Matchers.isIn(emails)))

                .andExpect(jsonPath("$[0].numberOfInsertedChars", Matchers.isIn(insertedCharsNumber)))
                .andExpect(jsonPath("$[1].numberOfInsertedChars", Matchers.isIn(insertedCharsNumber)))
                .andExpect(jsonPath("$[2].numberOfInsertedChars", Matchers.isIn(insertedCharsNumber)));

    }

    @Test
    public void shouldForbidAccessForNonVerifier()
            throws Exception {
        long transcriptionId = 1L;
        mvc.perform(
                get("/api/transcriptions/{transcriptionId}/verse-statistics", transcriptionId)
                        .header("Authorization", TOKEN_FOR_NON_VERIFIER))
                .andExpect(status().isForbidden());
    }



    private List<Integer> insertedCharsNumber = contents().stream()
            .map(TranscriptionVerseUserStatistic::getNumberOfInsertedChars)
            .map(Long::intValue)
            .collect(Collectors.toList());
    private List<String> emails = contents().stream().map(TranscriptionVerseUserStatistic::getEmail).collect(Collectors.toList());

    private List<TranscriptionVerseUserStatistic> contents(){return List.of(
            new TranscriptionVerseUserStatistic(0L, 0L, "email3@wp.pl", 29L),
            new TranscriptionVerseUserStatistic(0L, 0L, "email5@wp.pl", 6L),
            new TranscriptionVerseUserStatistic(0L, 0L, "email2@wp.pl", 3L));
    }


    @SneakyThrows
    private String toStringValue(TranscriptionVerseUserStatistic t){
        return mapper.writeValueAsString(t);
    }
}
