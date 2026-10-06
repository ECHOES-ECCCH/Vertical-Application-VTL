package pl.psnc.vtl.portal.controllers.transcriptions;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pl.psnc.vtl.portal.LogInService;
import pl.psnc.vtl.portal.model.verses.OcrMode;
import pl.psnc.vtl.portal.repositories.iiiftools.IIIFToolsRepository;
import pl.psnc.vtl.portal.services.eBook.Status;

import static org.junit.Assert.assertTrue;
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
public class TranscriptionOcrITCase {

    @Autowired
    private MockMvc mvc;

    private String TOKEN;
    private String TOKEN_FOR_USER_WITH_EXCEEDED_QUOTA;

    private static final String EMAIL = "email@wp.pl";

    private static final String PASSWORD = "myPassword";

    @InjectMocks
    IIIFToolsRepository iiifToolsRepository;


    @Before
    public void init()
            throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);
        TOKEN_FOR_USER_WITH_EXCEEDED_QUOTA = LogInService.getJWTToken(mvc, "userWithExceededQuota@wp.pl", "myPassword");
        ReflectionTestUtils.setField(iiifToolsRepository, "iiifToolsUrl", "http://localhost:6666/");
    }


    @Test
    public void shouldSuccessfullyRecognizeAllPages()
            throws Exception {
        long transcriptionId = 43L;

        mvc.perform(put("/api/transcriptions/{transcriptionId}/ocr", transcriptionId).param("lang", "pol").
                param("ocrMode", OcrMode.OVERRIDE.name()).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());

        String status = Status.PROCESSING.toString();
        JSONObject object = null;

        while (status.equalsIgnoreCase(Status.PROCESSING.toString())) {
            ResultActions result = mvc.perform(get("/api/transcriptions/{transcriptionId}/ocr-status", transcriptionId))
                    .
                            andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("status",
                            Matchers.anyOf(Matchers.is(Status.READY.toString()),
                                    Matchers.is(Status.PROCESSING.toString()))));

            //check if file is not empty
            String jsonString = result.andReturn().getResponse().getContentAsString();
            object = (JSONObject) JSONValue.parse(jsonString);
            status = object.get("status").toString();
        }

        mvc.perform(get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, 103L)
                .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.verses").isNotEmpty());

        mvc.perform(get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, 104L)
                .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.verses").isNotEmpty());

        mvc.perform(get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, 105L)
                .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.verses").isNotEmpty());
    }


    @Test
    public void shouldSuccessfullyRecognizeAllPagesTwice()
            throws Exception {
        long transcriptionId = 43L;

        mvc.perform(put("/api/transcriptions/{transcriptionId}/ocr", transcriptionId).param("lang", "pol")
                .param("ocrMode", OcrMode.OVERRIDE.name()).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());

        mvc.perform(put("/api/transcriptions/{transcriptionId}/ocr", transcriptionId).param("lang", "pol")
                .param("ocrMode", OcrMode.OVERRIDE.name()).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());

        String status = Status.PROCESSING.toString();
        JSONObject object = null;

        while (status.equalsIgnoreCase(Status.PROCESSING.toString())) {
            ResultActions result = mvc.perform(get("/api/transcriptions/{transcriptionId}/ocr-status", transcriptionId)
                    .header("Authorization", TOKEN)).
                    andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("status",
                    Matchers.anyOf(Matchers.is(Status.READY.toString()), Matchers.is(Status.PROCESSING.toString()))));

            //check if file is not empty
            String jsonString = result.andReturn().getResponse().getContentAsString();
            object = (JSONObject) JSONValue.parse(jsonString);
            status = object.get("status").toString();
        }

        mvc.perform(get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, 103L)
                .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.verses").isNotEmpty());

        mvc.perform(get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, 104L)
                .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.verses").isNotEmpty());

        mvc.perform(get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, 105L)
                .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(jsonPath("$.verses").isNotEmpty());
    }


    @Test
    public void a_shouldSuccessfullyRecognizeOnlyMissingPages()
            throws Exception {
        long transcriptionId = 43L;
        long page1 = 103L;
        long page2 = 104L;
        long page3 = 105L;
        String pages = "1-3";

        MvcResult mvcResult1 = mvc.perform(
                get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, page1)
                        .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        MvcResult mvcResult2 = mvc.perform(
                get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, page2)
                        .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        MvcResult mvcResult3 = mvc.perform(
                get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, page3)
                        .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.verses").isEmpty()).andReturn();

        mvc.perform(put("/api/transcriptions/{transcriptionId}/ocr", transcriptionId).param("lang", "eng")
                .param("ocrMode", OcrMode.EMPTYONLY.name()).param("pages", pages).header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());

        String status = Status.PROCESSING.toString();
        JSONObject object = null;

        while (status.equalsIgnoreCase(Status.PROCESSING.toString())) {
            ResultActions result = mvc.perform(get("/api/transcriptions/{transcriptionId}/ocr-status", transcriptionId))
                    .
                            andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("status",
                            Matchers.anyOf(Matchers.is(Status.READY.toString()),
                                    Matchers.is(Status.PROCESSING.toString()))));

            //check if file is not empty
            String jsonString = result.andReturn().getResponse().getContentAsString();
            object = (JSONObject) JSONValue.parse(jsonString);
            status = object.get("status").toString();
        }

        MvcResult mvcResult4 = mvc.perform(
                get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, page1)
                        .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        MvcResult mvcResult5 = mvc.perform(
                get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, page2)
                        .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        MvcResult mvcResult6 = mvc.perform(
                get("/api/transcriptions/{transcriptionId}/pages/{pageId}", transcriptionId, page3)
                        .header("Authorization", TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        Assert.assertEquals(mvcResult1.getResponse().getContentAsString(),
                mvcResult4.getResponse().getContentAsString());
        Assert.assertEquals(mvcResult2.getResponse().getContentAsString(),
                mvcResult5.getResponse().getContentAsString());
        Assert.assertNotEquals(mvcResult3.getResponse().getContentAsString(),
                mvcResult6.getResponse().getContentAsString());
    }


    @Test
    public void shouldReturnBadRequestWhenWrongPagesAreSupplied()
            throws Exception {
        long transcriptionId = 43L;

        mvc.perform(put("/api/transcriptions/{transcriptionId}/ocr", transcriptionId).param("lang", "pol").
                param("ocrMode", OcrMode.OVERRIDE.name()).param("pages", "1.5").header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());

        String status = Status.PROCESSING.toString();
        JSONObject object = null;

        while (status.equalsIgnoreCase(Status.PROCESSING.toString())) {
            ResultActions result = mvc.perform(get("/api/transcriptions/{transcriptionId}/ocr-status", transcriptionId))
                    .
                            andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("status",
                            Matchers.anyOf(Matchers.is(Status.READY.toString()),
                                    Matchers.is(Status.PROCESSING.toString()), Matchers.is(Status.FAILED.toString()))));

            //check if file is not empty
            String jsonString = result.andReturn().getResponse().getContentAsString();
            object = (JSONObject) JSONValue.parse(jsonString);
            status = object.get("status").toString();
        }

        assertTrue(status.equalsIgnoreCase(Status.FAILED.toString()));
    }


    @Test
    public void shouldReturnBadRequestWhenNonexistentPagesAreSupplied()
            throws Exception {
        long transcriptionId = 43L;

        mvc.perform(put("/api/transcriptions/{transcriptionId}/ocr", transcriptionId).param("lang", "pol")
                .param("ocrMode", OcrMode.OVERRIDE.name()).param("pages", "5,7").header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());

        String status = Status.PROCESSING.toString();
        JSONObject object = null;

        while (status.equalsIgnoreCase(Status.PROCESSING.toString())) {
            ResultActions result = mvc.perform(get("/api/transcriptions/{transcriptionId}/ocr-status", transcriptionId))
                    .
                            andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("status",
                            Matchers.anyOf(Matchers.is(Status.READY.toString()),
                                    Matchers.is(Status.PROCESSING.toString()), Matchers.is(Status.FAILED.toString()))));

            //check if file is not empty
            String jsonString = result.andReturn().getResponse().getContentAsString();
            object = (JSONObject) JSONValue.parse(jsonString);
            status = object.get("status").toString();
        }

        assertTrue(status.equalsIgnoreCase(Status.FAILED.toString()));
    }


    @Test
    public void shouldReturnBadRequestWhenNonexistentRangeOfPagesIsSupplied()
            throws Exception {
        long transcriptionId = 43L;

        mvc.perform(put("/api/transcriptions/{transcriptionId}/ocr", transcriptionId).param("lang", "pol")
                .param("ocrMode", OcrMode.OVERRIDE.name()).param("pages", "1-50000000").header("Authorization", TOKEN)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());

        String status = Status.PROCESSING.toString();
        JSONObject object = null;

        while (status.equalsIgnoreCase(Status.PROCESSING.toString())) {
            ResultActions result = mvc.perform(get("/api/transcriptions/{transcriptionId}/ocr-status", transcriptionId))
                    .
                            andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("status",
                            Matchers.anyOf(Matchers.is(Status.READY.toString()),
                                    Matchers.is(Status.PROCESSING.toString()), Matchers.is(Status.FAILED.toString()))));

            //check if file is not empty
            String jsonString = result.andReturn().getResponse().getContentAsString();
            object = (JSONObject) JSONValue.parse(jsonString);
            status = object.get("status").toString();
        }

        assertTrue(status.equalsIgnoreCase(Status.FAILED.toString()));
    }

}
