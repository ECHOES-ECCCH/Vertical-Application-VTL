package pl.psnc.vtl.portal.services.eBook;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.transaction.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.psnc.vtl.portal.conf.logging.Slf4jMDCFilter.DEFAULT_MDC_UUID_TOKEN_KEY;
import static pl.psnc.vtl.portal.conf.logging.Slf4jMDCFilter.DEFAULT_RESPONSE_TOKEN_HEADER;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "vtl.iiif-server-url=http://iiif-server/")
@Transactional
public class PdfExporterITCase {

    @Autowired
    private PdfExporter pdfExporter;

    @Autowired
    MockMvc mvc;

    @Ignore("Test wykonuje się >2min")
    @Test
    public void shouldReturnPdfLocation() {
        long transcriptionId = 1L;
        String hash = "someHash";
        pdfExporter.exportEbook(new EbookDto(Ebook.PDF, transcriptionId, 1L, false, null), hash);
    }


    @Ignore("Test wykonuje się w nieskończoność")
    @Test
    public void shouldCreatePdf()
            throws Exception {
        String location = mvc.perform(post("/api/transcriptions/{id}/eBooks", 1L).param("format", Ebook.PDF.toString()))
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
}
