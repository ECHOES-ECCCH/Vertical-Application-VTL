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
import pl.psnc.vtl.portal.dto.transcriptions.TranscriptionCore;
import pl.psnc.vtl.portal.model.imports.FileType;
import pl.psnc.vtl.portal.model.pages.Page;
import pl.psnc.vtl.portal.model.regions.BoundingBox;
import pl.psnc.vtl.portal.model.transcriptions.CreationMode;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.services.domains.DomainService;

import java.net.URL;
import java.util.AbstractMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@Transactional
@ActiveProfiles("test")
public class ImportsControllerManifestITCase {

    @Autowired
    private MockMvc mvc;
    private String TOKEN;
    ObjectMapper objectMapper = new ObjectMapper();
    private static final String EMAIL = "email@wp.pl";
    private static final String PASSWORD = "myPassword";
    private final String ANALYTIC_DC_WITH_JSON_MANIFEST = "METSLinksWithJSONManifest.xml";
    private final String MANIFEST = "Manifest.json";

    @MockBean
    DomainService domainService;

    Map<Integer, BoundingBox> pagesWithBoxes = Map
            .ofEntries(new AbstractMap.SimpleEntry<>(1, new BoundingBox(0, 0, 10, 110)),
                    new AbstractMap.SimpleEntry<>(2, new BoundingBox(0, 0, 20, 120)),
                    new AbstractMap.SimpleEntry<>(3, new BoundingBox(0, 0, 30, 130)),
                    new AbstractMap.SimpleEntry<>(4, new BoundingBox(0, 0, 40, 140)));

    public WireMockServer wireMockServer = new WireMockServer();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(4354);


    @Before
    public void init()
            throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void shouldCreateTranscriptionFromMetsAndDirectManifest()
            throws Exception {
        String xml = new String(
                getClass().getClassLoader().getResourceAsStream(ANALYTIC_DC_WITH_JSON_MANIFEST).readAllBytes());
        String manifestJson = new String(getClass().getClassLoader().getResourceAsStream(MANIFEST).readAllBytes());
        Mockito.doNothing().when(domainService).validateDomain(any());

        String urlString = "http://localhost:4354/lru-modnar/test-direct.xml";
        URL resourceUrl = new URL(urlString);

        String manifestURL = "http://localhost:4354/minifest-direct";
        URL manifestUrl = new URL(manifestURL);

        wireMockRule.stubFor(
                get(urlMatching(manifestUrl.getPath())).willReturn(aResponse().withStatus(200).withBody(manifestJson)));

        wireMockRule
                .stubFor(get(urlMatching(resourceUrl.getPath())).willReturn(aResponse().withStatus(200).withBody(xml)));

        ImportTranscriptionDTO dto = new ImportTranscriptionDTO(urlString, FileType.METS, null);

        MvcResult response = mvc.perform(
                MockMvcRequestBuilders.put("/api/imports").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", TOKEN).content(objectMapper.writeValueAsString(dto))).andReturn();

        TranscriptionCore transcriptionCore = objectMapper
                .readValue(response.getResponse().getContentAsString(), TranscriptionCore.class);

        MvcResult transcriptionResponse = mvc.perform(
                MockMvcRequestBuilders.post("/api/imports").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", TOKEN).content(objectMapper.writeValueAsString(transcriptionCore)))
                .andReturn();

        String location = transcriptionResponse.getResponse().getHeader("Location");

        MvcResult result = mvc.perform(MockMvcRequestBuilders.get(location).header("Authorization", TOKEN)).andReturn();

        Transcription t = objectMapper.readValue(result.getResponse().getContentAsString(), Transcription.class);
        assertEquals(4, t.getPages().getList().size());
        assertEquals(CreationMode.METS, t.getCreationMode());
        t.getPages().getList().forEach(this::pageTest);
    }

    private void pageTest(Page page) {
        assertFalse(page.getPath().isEmpty());
        assertFalse(page.getThumbnailUrl().isEmpty());
        assertEquals(pagesWithBoxes.get(page.getPageNo()), page.getBoundingBox());
    }
}
