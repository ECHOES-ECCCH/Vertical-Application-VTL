package pl.psnc.vtl.portal.repositories.iiiftools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import pl.psnc.vtl.portal.model.statics.StaticFileLanguage;
import pl.psnc.vtl.portal.model.users.User;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class IIIFToolsRepositoryTest {


    @InjectMocks
    IIIFToolsRepository repository;


    WireMockServer wireMockServer = new WireMockServer();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(6666);

    ObjectMapper mapper = new ObjectMapper();


    @Before
    public void init() {
        ReflectionTestUtils.setField(repository, "iiifToolsUrl", "http://localhost:6666/");
    }


    @Test(expected = FailedToSavePagesException.class)
    public void shouldThrowFailedToSavePagesException()
            throws MalformedURLException, JsonProcessingException {
        Long transcriptionId = 1L;
        List<String> links = List.of("l1", "l2");
        Map<String, String> linksFileNames = Map.of("l1", "f1", "l2", "f2");

        String responseBody = mapper.writeValueAsString(linksFileNames);
        String requestBody = mapper.writeValueAsString(links);

        String urlString = "http://localhost:6666/api/iiif/" + transcriptionId + "/download-images";
        URL url = new URL(urlString);

        stubFor(post(urlMatching(url.getPath())).withRequestBody(equalTo(requestBody))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(500)));

        Map<String, String> map = repository.saveTranscriptionFiles(transcriptionId, links, null).getUrlsToFilenames();
    }


    @Test
    public void shouldReturnLinksFileNamesMap()
            throws JsonProcessingException, MalformedURLException {
        Long transcriptionId = 1L;
        List<String> links = List.of("l1", "l2");
        Map<String, String> linksFileNames = Map.of("l1", "f1", "l2", "f2");
        int quota = 0;
        DownloadFilesResponse downloadFilesResponse = new DownloadFilesResponse(linksFileNames, quota);

        String responseBody = mapper.writeValueAsString(downloadFilesResponse);
        String requestBody = mapper.writeValueAsString(links);

        String urlString = "http://localhost:6666/api/iiif/" + transcriptionId + "/download-images";
        URL url = new URL(urlString);

        stubFor(post(urlMatching(url.getPath())).withRequestBody(equalTo(requestBody)).willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBody(responseBody).withStatus(201)));

        Map<String, String> map = repository.saveTranscriptionFiles(transcriptionId, links, null).getUrlsToFilenames();
        assertEquals(2, map.size());
        assertTrue(map.containsKey("l1"));
        assertTrue(map.containsKey("l2"));

    }

    @Test(expected = IIIFToolsException.class)
    public void shouldSendMailWhenThereIsIIIFToolsException()
            throws Exception {
        Long transcriptionId = 1L;

        String urlString = "http://localhost:6666/api/iiif/" + transcriptionId;
        URL url = new URL(urlString);
        String responseBody = " {  \"timestamp\": null, \"status\": 403, \"error\": \"IOException\"}";

        stubFor(delete(urlMatching(url.getPath())).willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBody(responseBody).withStatus(403)));

        String email = "userEmail@wp.pl";

        List<User> usersList = new ArrayList<>();
        User user = new User();
        user.setEmail(email);
        usersList.add(user);

        StaticFileLanguage lang = StaticFileLanguage.PL;

        repository.deleteTranscriptionImages(transcriptionId);
    }


    @Test
    public void shouldDeleteTranscriptionImages()
            throws IOException {
        long transcriptionId = 1L;
        String urlString = "http://localhost:6666/api/iiif/" + transcriptionId;

        URL url = new URL(urlString);

        stubFor(delete(urlMatching(url.getPath()))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(200)));

        repository.deleteTranscriptionImages(transcriptionId);
    }

}