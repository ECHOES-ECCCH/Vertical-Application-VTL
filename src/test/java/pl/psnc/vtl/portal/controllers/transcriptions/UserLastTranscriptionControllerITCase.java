package pl.psnc.vtl.portal.controllers.transcriptions;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.LogInService;
import pl.psnc.vtl.portal.conf.TestJsonMapper;
import pl.psnc.vtl.portal.dto.transcriptions.*;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.model.verses.Heading;
import pl.psnc.vtl.portal.repositories.iiiftools.IIIFToolsRepository;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
@Transactional
public class UserLastTranscriptionControllerITCase {

    @Autowired
    private MockMvc mvc;

    private String TOKEN;

    private static final String EMAIL = "email@wp.pl";

    private static final String PASSWORD = "myPassword";

    @InjectMocks
    IIIFToolsRepository iiifToolsRepository;


    @Before
    public void init()
            throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);

        ReflectionTestUtils.setField(iiifToolsRepository, "iiifToolsUrl", "http://localhost:6666/");
    }


    @Test
    public void shouldReturnUnauthorizedUserLastTranscriptionWhenNoLoggedUser()
            throws Exception {

        mvc.perform(get("/api/my-last-transcription").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }


    private TranscriptionCore getTranscriptionCore() {
        TranscriptionCore newTranscription = new TranscriptionCore();

        //analyticInfo
        AnalyticInfoCore analyticInfo = new AnalyticInfoCore();
        String analyticInfoTitle = "analyticInfoTitle";
        analyticInfo.setTitle(analyticInfoTitle);

        newTranscription.setAnalyticInfo(analyticInfo);
        newTranscription.setAcceptedRegulations(true);

        return newTranscription;
    }


    @Ignore
    @Test
    public void shouldReturnUserLastTranscriptionWhenUserDidNotWorkOnSomeTranscription()
            throws Exception {
        //when
        String token = LogInService.getJWTToken(mvc, "email1@wp.pl", PASSWORD);

        //generate new transcription
        TranscriptionCore newTranscription = getTranscriptionCore();

        String jsonResponse = mvc.perform(post("/api/transcriptions", newTranscription).header("Authorization", token)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(newTranscription))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString();

        Transcription transcription = TestJsonMapper.serializingObjectMapper()
                .readValue(jsonResponse, Transcription.class);

        //add user to transcription
        mvc.perform(post("/api/transcriptions/{transcriptionId}/allowed-users", transcription.getId())
                .header("Authorization", token).content(EMAIL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        //then
        mvc.perform(get("/api/my-last-transcription").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", is(transcription.getId().intValue())));
    }


    @Test
    public void shouldReturnUserLastTranscriptionWhenUserWorkedOnSomeTranscription()
            throws Exception {
        //when
        int verseId = 1;
        Heading heading = Heading.END_OF_HEADING_1;

        String token = LogInService.getJWTToken(mvc, "email@wp.pl", PASSWORD);

        mvc.perform(put("/api/verses/{verseId}/formatting/heading/{heading}", verseId, heading)
                .header("Authorization", token)).andExpect(status().isOk())
                .andExpect(jsonPath("heading", is("end-of-heading-1")));

        //then
        mvc.perform(get("/api/my-last-transcription").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", is(1)));
    }

    @Test
    public void shouldNotReturnUserLastTranscriptionWhenUserWorkedOnSomeTranscriptionAndTranscriptionWasDeleted()
            throws Exception {
        //when
        //add transcription
        TranscriptionCore newTranscription = getTranscriptionCore();

        String jsonResponse = mvc.perform(post("/api/transcriptions", newTranscription).header("Authorization", TOKEN)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(newTranscription))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString();

        Transcription transcription = TestJsonMapper.serializingObjectMapper()
                .readValue(jsonResponse, Transcription.class);

        mvc.perform(get("/api/my-last-transcription").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", is(transcription.getId().intValue())));

        mvc.perform(delete("/api/transcriptions/{id}", transcription.getId()).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());


        //then
        mvc.perform(get("/api/my-last-transcription").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("id", not(transcription.getId().intValue())));


    }


    @Test
    public void shouldReturnUserLastTranscriptionWhenNoLoggedUser()
            throws Exception {

        //add transcription
        TranscriptionCore newTranscription = getTranscriptionCore();

        String jsonResponse = mvc.perform(post("/api/transcriptions", newTranscription).header("Authorization", TOKEN)
                .content(TestJsonMapper.serializingObjectMapper().writeValueAsString(newTranscription))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString();

        Transcription transcription = TestJsonMapper.serializingObjectMapper()
                .readValue(jsonResponse, Transcription.class);

        //make transcription public
        mvc.perform(put("/api/transcriptions/{transcriptionId}/hidden", transcription.getId()).content("false")
                .contentType(MediaType.APPLICATION_JSON_VALUE).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("hidden", is(false)));

        //should return
        mvc.perform(get("/api/last-transcriptions").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(transcription.getId().intValue())));
    }
}