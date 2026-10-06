package pl.psnc.vtl.portal.controllers.transcriptions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import pl.psnc.vtl.portal.LogInService;
import pl.psnc.vtl.portal.conf.TestJsonMapper;
import pl.psnc.vtl.portal.dto.transcriptions.statistics.TranscriptionStatistics;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionSummarize;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TranscriptionStatisticsControllerITCase {

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
    public void shouldReturnTranscriptionStatistics()
            throws Exception {
        long transcriptionId = 1L;

        String contentAsString = mvc.perform(
                get("/api/transcriptions/{transcriptionId}/statistics", transcriptionId).header("Authorization", TOKEN)
                        .param("page", "1").param("perPage", "3")).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString();

        TranscriptionStatistics transcriptionStatistics = TestJsonMapper.serializingObjectMapper()
                .readValue(contentAsString, TranscriptionStatistics.class);
        assertThat(transcriptionStatistics.getLayersStatistics().get(0).getAllVerses(), is(31L));
        assertThat(transcriptionStatistics.getLayersStatistics().get(0).getVersesTranscribed(), is(0L));
        assertThat(transcriptionStatistics.getLayersStatistics().get(0).getVersesMarkedAsVerified(), is(0L));
        assertThat(transcriptionStatistics.getLayersStatistics().get(0).getVersesMarkedAsIncorrect(), is(0L));
        assertThat(transcriptionStatistics.getPaginatedPagesStatistics().get(0).getHits(), is(3L));
        assertThat(transcriptionStatistics.getPaginatedPagesStatistics().get(0).getCount(), is(3));
        assertThat(transcriptionStatistics.getPaginatedPagesStatistics().get(0).getPage(), is(1));
        assertThat(transcriptionStatistics.getPaginatedPagesStatistics().get(0).getPerPage(), is(3));
        assertThat(transcriptionStatistics.getPaginatedPagesStatistics().get(0).getPages(), is(1));
        assertThat(
                transcriptionStatistics.getPaginatedPagesStatistics().get(0).getPageStatisticsList().get(0).getVersesInFile(),
                is(5L));
    }

    @Test
    public void shouldReturnTranscriptionSummarize() throws Exception {
        long transcriptionId = 1L;
        String contentAsString = mvc.perform(
                get("/api/transcriptions/{transcriptionId}/summarize", transcriptionId)
                        .header("Authorization", TOKEN).param("transcriptionId", "1"))
                .andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString();

        TranscriptionSummarize[] transcriptionStatistics = TestJsonMapper.serializingObjectMapper()
                .readValue(contentAsString, TranscriptionSummarize[].class);
        assert transcriptionStatistics.length == 6;
        assert Arrays.stream(transcriptionStatistics).reduce(0L,
                (subtotal, element) -> subtotal + element.getAccepted(),Long::sum
        )==0L;
        assert Arrays.stream(transcriptionStatistics).reduce(0L,
                (subtotal, element) -> subtotal + element.getCorrected(),Long::sum
        )==4L;
        assert Arrays.stream(transcriptionStatistics).reduce(0L,
                (subtotal, element) -> subtotal + element.getRejected(),Long::sum
        )==0L;
        assert Arrays.stream(transcriptionStatistics).reduce(0L,
                (subtotal, element) -> subtotal + element.getUserRejected(),Long::sum
        )==0L;
    }


    @Test
    public void shouldReturnTranscriptionStatisticsDefaultParameters()
            throws Exception {
        long transcriptionId = 1L;

        String contentAsString = mvc.perform(
                get("/api/transcriptions/{transcriptionId}/statistics", transcriptionId).header("Authorization", TOKEN))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        TranscriptionStatistics transcriptionStatistics = TestJsonMapper.serializingObjectMapper()
                .readValue(contentAsString, TranscriptionStatistics.class);
        assertThat(transcriptionStatistics.getPaginatedPagesStatistics().get(0).getPage(), is(1));
        assertThat(transcriptionStatistics.getPaginatedPagesStatistics().get(0).getPerPage(), is(10));
    }


    @Test
    public void shouldThrowEntityNotFoundForNotExistingTranscription()
            throws Exception {
        long transcriptionId = -1L;

        mvc.perform(
                get("/api/transcriptions/{transcriptionId}/statistics", transcriptionId).header("Authorization", TOKEN))
                .andExpect(status().isNotFound());
    }

}
