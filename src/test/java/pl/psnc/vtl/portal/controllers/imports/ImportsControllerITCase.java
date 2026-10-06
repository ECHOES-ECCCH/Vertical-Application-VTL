package pl.psnc.vtl.portal.controllers.imports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.LogInService;
import pl.psnc.vtl.portal.dto.imports.ImportTranscriptionDTO;
import pl.psnc.vtl.portal.model.imports.FileType;

import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@ActiveProfiles("test")
@Transactional
public class ImportsControllerITCase {

    @Autowired
    private MockMvc mvc;
    private String TOKEN;
    ObjectMapper objectMapper = new ObjectMapper();
    private static final String EMAIL = "email@wp.pl";
    private static final String PASSWORD = "myPassword";
    private final String XML = "DublinCoreAcceptanceTest.xml";

    WireMockServer wireMockServer = new WireMockServer();
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(6666);

    @Before
    public void init()
            throws Exception {
        TOKEN = LogInService.getJWTToken(mvc, EMAIL, PASSWORD);
        objectMapper.registerModule( new JavaTimeModule());
    }


    //Test for whole flow , with https and redirection... whole shabam ... hope it will serve someone

    //Test is ignored due to problem with WireMock server mocking. when two test use server stubbing results in conflict ,
    //if somebody knows how to solve stubbing conflict , please do it and remove ignore
    @Ignore
    @Test
    public void shouldReturnTranscriptionCoreForMETS() throws Exception {
        String xml = new String(getClass().getClassLoader().getResourceAsStream(XML).readAllBytes());
        String urlString = "http://localhost:6666/resourceUrl";
        String redirectUrlString = "http://localhost:6666/redirectUrl";

        URL resourceUrl = new URL(urlString);
        URL redirectUrl = new URL(redirectUrlString);

        stubFor(get(urlMatching(resourceUrl.getPath()))
                .willReturn(seeOther(redirectUrl.getPath())));
        stubFor(get(urlMatching(resourceUrl.getPath()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(xml)));

        ImportTranscriptionDTO dto = new ImportTranscriptionDTO(urlString, FileType.METS, null);

        String summaryString = "firstLine" + System.lineSeparator() + "secondLine" + System.lineSeparator() + "thirdLine";
        mvc.perform(MockMvcRequestBuilders.put("/api/imports")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", TOKEN)
                .content(objectMapper.writeValueAsString(dto)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("analyticInfo.title", is("title")))
                .andExpect(jsonPath("analyticInfo.actors.length()", is(2)))
                .andExpect(jsonPath("analyticInfo.language.code", is("pol")))
                .andExpect(jsonPath("monographicInfo.isbn", is("ISbN text")))
                .andExpect(jsonPath("monographicInfo.eisbn", is("EiSbN text")))
                .andExpect(jsonPath("monographicInfo.issn", is("ISsn żźŻńłęó")))
                .andExpect(jsonPath("monographicInfo.eissn", is("EiSsN text")))
                .andExpect(jsonPath("profileDesc.summary", is(summaryString)))
                .andExpect(jsonPath("monographicInfo.publicationDate", is("publicationDate")))
                .andExpect(jsonPath("monographicInfo.publicationDate", is("publicationDate")))
                .andExpect(jsonPath("monographicInfo.title", is("relationTitle")))
                .andExpect(jsonPath("profileDesc.keywords.length()", is(2)))
                .andExpect(jsonPath("fileType", is(FileType.METS.getValue())))
                .andExpect(jsonPath("resourceUrl", is(urlString)));
    }

}