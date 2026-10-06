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
import pl.psnc.vtl.portal.model.transcriptions.CreationMode;
import pl.psnc.vtl.portal.model.transcriptions.Transcription;
import pl.psnc.vtl.portal.services.domains.DomainService;

import java.net.URL;

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
public class ImportsControllerLinkedPublicationITCase {

    @Autowired
    private MockMvc mvc;
    private String TOKEN;
    ObjectMapper objectMapper = new ObjectMapper();
    private static final String EMAIL = "email@wp.pl";
    private static final String PASSWORD = "myPassword";
    private final String LINKED_PUBLICATION = "LinkedPublication.xml" ;
    private final String MANIFEST = "ManifestLinked.json";
    private final String LINKED_XML_PAGE = "PageLinkedToLinkedPublication.xml";

    @MockBean
    DomainService domainService;


    public WireMockServer wireMockServer = new WireMockServer();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8787);

    @Before
    public void init()
            throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);
        objectMapper.registerModule( new JavaTimeModule());
    }

    @Test
    public void shouldCreateTranscriptionFromMetsAndLinkedManifest() throws Exception {
        String xml = new String(getClass().getClassLoader().getResourceAsStream(LINKED_PUBLICATION).readAllBytes());
        String manifestJson =  new String(getClass().getClassLoader().getResourceAsStream(MANIFEST).readAllBytes());
        String linkedXml = new String(getClass().getClassLoader().getResourceAsStream(LINKED_XML_PAGE).readAllBytes());
        Mockito.doNothing().when(domainService).validateDomain(any());

        String urlString = "http://localhost:8787/ramdom-url/test-linked.xml";
        URL resourceUrl = new URL(urlString);

        String linkedXmlString = "http://localhost:8787/linkedPublication";
        URL linkedXmlUrl = new URL(linkedXmlString);

        String manifestURL = "http://localhost:8787/minifest-linked";
        URL manifestUrl = new URL(manifestURL);

        wireMockRule.stubFor(get(urlMatching(manifestUrl.getPath()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(manifestJson)));

        wireMockRule.stubFor(get(urlMatching(resourceUrl.getPath()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(xml)));

        wireMockRule.stubFor(get(urlMatching(linkedXmlUrl.getPath()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(linkedXml)));

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
        assertEquals(3, t.getPages().getList().size());
        assertEquals(CreationMode.METS, t.getCreationMode());
        t.getPages().getList().forEach(this::pageTest);
    }

    private void pageTest(Page page){
        assertFalse(page.getPath().isEmpty());
        assertFalse(page.getThumbnailUrl().isEmpty());
    }
}
