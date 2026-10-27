package pl.psnc.vtl.portal.controllers.eBook;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import pl.psnc.vtl.portal.services.eBook.Ebook;
import pl.psnc.vtl.portal.services.eBook.Status;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.psnc.vtl.portal.conf.logging.Slf4jMDCFilter.DEFAULT_MDC_UUID_TOKEN_KEY;
import static pl.psnc.vtl.portal.conf.logging.Slf4jMDCFilter.DEFAULT_RESPONSE_TOKEN_HEADER;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Slf4j
@TestPropertySource(properties = "vtl.iiif-server-url=http://iiif-server/")
public class EBookExportControllerITCase {

    @Autowired
    MockMvc mvc;


    @Test
    public void shouldReturnCorrectResourceLocation()
            throws Exception {
        mvc.perform(post("/api/transcriptions/{id}/eBooks", 1L).contentType(MediaType.APPLICATION_JSON)
                .param("format", Ebook.MOBI.toString()))

                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(header().string("Location", Matchers.startsWith("/api/transcriptions/eBooks/")));
    }


    @Test
    public void shouldReturnEBook()
            throws Exception {
        String location = mvc
                .perform(post("/api/transcriptions/{id}/eBooks", 1L).param("format", Ebook.MOBI.toString())).andReturn()
                .getResponse().getHeader("Location");

        mvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("status", Matchers.notNullValue()));
    }


    @Test
    public void shouldReturnBadRequestWhenHashDoesNotExists()
            throws Exception {
        mvc.perform(post("/api/transcriptions/{id}/eBooks", 1L).param("format", Ebook.MOBI.toString())).andReturn()
                .getResponse().getHeader("Location");

        mvc.perform(get("api/transcriptions/eBooks/{hash}", "nonExistingHash")).andExpect(status().isNotFound());
    }


    @Test
    public void shouldReturnCorrectResourceLocationForEpub()
            throws Exception {
        mvc.perform(post("/api/transcriptions/{id}/eBooks", 1L).contentType(MediaType.APPLICATION_JSON)
                .param("format", Ebook.EPUB.toString()))

                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(header().string("Location", Matchers.startsWith("/api/transcriptions/eBooks/")));
    }


    @Test
    public void shouldReturnCorrectResourceLocationForMobi()
            throws Exception {
        mvc.perform(post("/api/transcriptions/{id}/eBooks", 1L).contentType(MediaType.APPLICATION_JSON)
                .param("format", Ebook.MOBI.toString()))

                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(header().string("Location", Matchers.startsWith("/api/transcriptions/eBooks/")));
    }


    @Test
    public void shouldReturnEBookForEpub()
            throws Exception {

        String location = mvc.perform(post("/api/transcriptions/{id}/eBooks", 1L).param("format", Ebook.EPUB.toString()))
                .andReturn().getResponse().getHeader("Location");

        String status = Status.PROCESSING.toString();
        JSONObject object = null;

        while (status.equalsIgnoreCase(Status.PROCESSING.toString())) {
            ResultActions result = mvc.perform(get(location)).andExpect(status().isOk()).andExpect(MockMvcResultMatchers
                    .jsonPath("status", Matchers.anyOf(Matchers.is(Status.READY.toString()),
                            Matchers.is(Status.PROCESSING.toString()))));


            //check if file is not empty
            String jsonString = result.andReturn().getResponse().getContentAsString();
            object = (JSONObject) JSONValue.parse(jsonString);
            status = object.get("status").toString();

        }
        String iiifServerFileUrl = object.get("details").toString();

        HttpRequest request = HttpRequest.newBuilder()
                .setHeader(DEFAULT_RESPONSE_TOKEN_HEADER, MDC.get(DEFAULT_MDC_UUID_TOKEN_KEY))
                .uri(URI.create(iiifServerFileUrl))
                .GET().build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().getBytes().length > 0);
    }


    @Test
    public void shouldReturnEBookForMobi()
            throws Exception {

        String location = mvc
                .perform(post("/api/transcriptions/{id}/eBooks", 1L).param("format", Ebook.MOBI.toString())).andReturn()
                .getResponse().getHeader("Location");

        String status = Status.PROCESSING.toString();
        JSONObject object = null;

        while (status.equalsIgnoreCase(Status.PROCESSING.toString())) {
            ResultActions result = mvc.perform(get(location)).andExpect(status().isOk()).andExpect(MockMvcResultMatchers
                    .jsonPath("status", Matchers.anyOf(Matchers.is(Status.READY.toString()),
                            Matchers.is(Status.PROCESSING.toString()))));

            //check if file is not empty
            String jsonString = result.andReturn().getResponse().getContentAsString();
            object = (JSONObject) JSONValue.parse(jsonString);
            status = object.get("status").toString();
        }
        String iiifServerFileUrl = object.get("details").toString();

        HttpRequest request = HttpRequest.newBuilder()
                .setHeader(DEFAULT_RESPONSE_TOKEN_HEADER, MDC.get(DEFAULT_MDC_UUID_TOKEN_KEY))
                .uri(URI.create(iiifServerFileUrl))
                .GET().build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().getBytes().length > 0);
    }


    @Test
    public void shouldReturnBadRequestWhenHashDoesNotExistsForEpub()
            throws Exception {
        mvc.perform(post("/api/transcriptions/{id}/eBooks", 1L).param("format", Ebook.EPUB.toString())).andReturn()
                .getResponse().getHeader("Location");

        mvc.perform(get("api/transcriptions/eBooks/{hash}", "nonExistingHash")).andExpect(status().isNotFound());
    }


    @Test
    public void shouldReturnBadRequestWhenHashDoesNotExistsForMobi()
            throws Exception {
        mvc.perform(post("/api/transcriptions/{id}/eBooks", 1L).param("format", Ebook.MOBI.toString())).andReturn()
                .getResponse().getHeader("Location");

        mvc.perform(get("api/transcriptions/eBooks/{hash}", "nonExistingHash")).andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnPfdForTranscription()
            throws Exception {
        String location = mvc
                .perform(post("/api/transcriptions/{id}/eBooks", 1L).param("format", Ebook.PDF.toString())).andReturn()
                .getResponse().getHeader("Location");

        mvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("status", Matchers.notNullValue()));
    }
}