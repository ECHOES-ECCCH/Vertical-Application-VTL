package pl.psnc.vtl.portal.controllers.imports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.LogInService;
import pl.psnc.vtl.portal.dto.imports.ImportTranscriptionDTO;
import pl.psnc.vtl.portal.dto.transcriptions.AnalyticInfoCore;
import pl.psnc.vtl.portal.dto.transcriptions.ProfileDescCore;
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;
import pl.psnc.vtl.portal.model.imports.FileType;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.transcriptions.CreationMode;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.services.domains.DomainService;

import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@Transactional
@ActiveProfiles("test")
public class ImportsControllerE2EITCase {

    @Autowired
    private MockMvc mvc;
    private String TOKEN;
    private String TOKEN_FOR_USER_WITH_EXCEEDED_QUOTA;
    ObjectMapper objectMapper = new ObjectMapper();
    private static final String EMAIL = "email@wp.pl";
    private static final String PASSWORD = "myPassword";
    private final String IMPORT_TRANSCRIPTION_E2E = "ImportTranscriptionE2E.xml";

    @MockBean
    DomainService domainService;
    WireMockServer wireMockServer = new WireMockServer();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(1112);

    @Before
    public void init()
            throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);
        TOKEN_FOR_USER_WITH_EXCEEDED_QUOTA = LogInService.getJWTToken(mvc, "userWithExceededQuota@wp.pl", "myPassword");
        objectMapper.registerModule( new JavaTimeModule());
    }

    @Test
    public void shouldCreateTranscriptionFromMets() throws Exception {
        String xml = new String(getClass().getClassLoader().getResourceAsStream(IMPORT_TRANSCRIPTION_E2E).readAllBytes());
        String urlString = "http://localhost:1112/pro/leopoldina/mets/test1.xml";
        Mockito.doNothing().when(domainService).validateDomain(any());
        URL resourceUrl = new URL(urlString);
        stubFor(get(urlMatching(resourceUrl.getPath()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(xml)));

        ImportTranscriptionDTO dto = new ImportTranscriptionDTO(urlString, FileType.METS, null);

        MvcResult response = mvc.perform(MockMvcRequestBuilders.put("/api/imports")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TOKEN)
                .content(objectMapper.writeValueAsString(dto)))
                .andReturn();

        TranscriptionCore transcriptionCore = objectMapper.readValue(response.getResponse().getContentAsString(), TranscriptionCore.class);
        MvcResult transcriptionResponse = mvc.perform(MockMvcRequestBuilders.post("/api/imports")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TOKEN)
                .content(objectMapper.writeValueAsString(transcriptionCore)))
                .andReturn();

        String location = transcriptionResponse.getResponse().getHeader("Location");

        MvcResult result = mvc.perform(MockMvcRequestBuilders.get(location)
                .header("Authorization", TOKEN)).andReturn();

        Transcription t = objectMapper.readValue(result.getResponse().getContentAsString(), Transcription.class);
        assertEquals(8, t.getPages().getList().size());
        assertEquals(CreationMode.METS, t.getCreationMode());
        t.getPages().getList().forEach(this::pageTest);
    }

    private void pageTest(Page page){
        assertFalse(page.getPath().isEmpty());
        assertFalse(page.getThumbnailUrl().isEmpty());
    }

    @Test
    public void shouldThrowQuotaExceededException() throws Exception {
        String xml = new String(getClass().getClassLoader().getResourceAsStream(IMPORT_TRANSCRIPTION_E2E).readAllBytes());
        String urlString = "http://localhost:1112/pro/leopoldina/mets/test1.xml";
        URL resourceUrl = new URL(urlString);
        stubFor(get(urlMatching(resourceUrl.getPath()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(xml)));

        TranscriptionCore transcriptionCore = new TranscriptionCore();
        transcriptionCore.setFileType(FileType.METS);
        transcriptionCore.setAnalytic(false);
        transcriptionCore.setExternalUrl("http://localhost:1112/pro/leopoldina/mets/test1.xml");
        transcriptionCore.setAnalyticInfo(new AnalyticInfoCore());
        transcriptionCore.setProfileDesc(new ProfileDescCore());
        mvc.perform(MockMvcRequestBuilders.post("/api/imports")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TOKEN_FOR_USER_WITH_EXCEEDED_QUOTA)
                .content(objectMapper.writeValueAsString(transcriptionCore)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code", is("quota.error.exceeded")));


    }
}
