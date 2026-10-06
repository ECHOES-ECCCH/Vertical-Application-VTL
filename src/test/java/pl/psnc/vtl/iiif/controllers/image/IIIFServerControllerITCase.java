package pl.psnc.vtl.iiif.controllers.image;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class IIIFServerControllerITCase {

    @Autowired
    private MockMvc mvc;

    ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void shouldMakePyramidFromTiff()
            throws Exception {
        Integer transcriptionId = 1;
        String filename = "page0250.tif";

        mvc.perform(put("/api/iiif/{transcriptionId}/make-pyramid/{filename}", transcriptionId, filename)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }


    @Test
    public void shouldntMakePyramidFromNonExistentImage()
            throws Exception {
        Integer transcriptionId = 1;
        String filename = "non-existent.tif";

        mvc.perform(put("/api/iiif/{transcriptionId}/make-pyramid/{filename}", transcriptionId, filename)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isInternalServerError());
    }


    @Test
    public void shouldMakePyramidFromPdf()
            throws Exception {
        Integer transcriptionId = 1;
        String filename = "document.pdf";

        mvc.perform(put("/api/iiif/{transcriptionId}/make-pyramid/{filename}", transcriptionId, filename)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }


    @Test
    public void shouldntMakePyramidFromNonExistentPdf()
            throws Exception {
        Integer transcriptionId = 1;
        String filename = "non-existent.pdf";

        mvc.perform(put("/api/iiif/{transcriptionId}/make-pyramid/{filename}", transcriptionId, filename)
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isInternalServerError());
    }


    @Test
    public void shouldCopyURLToFile()
            throws Exception {
        long transcriptionId = 2L;
        List<String> urls = new ArrayList<>();
        urls.add("https://www.motogama.pl/pdf_files/katalogipdf/1.pdf");
        mvc.perform(post("/api/iiif/{transcriptionId}/download-images", transcriptionId)
                .content(objectMapper.writeValueAsString(urls)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urlsToFilenames", hasKey("https://www.motogama.pl/pdf_files/katalogipdf/1.pdf")))
                .andExpect(jsonPath("$.urlsToFilenames", hasValue("1.pdf")));
    }


    @Test
    public void shouldThrowParseUrlException()
            throws Exception {
        long transcriptionId = 3L;
        List<String> urls = new ArrayList<>();
        urls.add("https://www.motogama.pl/pdf_files/katalogipdf/1.pdf");
        urls.add("bad-url");
        mvc.perform(post("/api/iiif/{transcriptionId}/download-images", transcriptionId)
                .content(objectMapper.writeValueAsString(urls)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void shouldRemoveDirectoryAndThrowCopyURLsToFilesException()
            throws Exception {
        long transcriptionId = 2L;
        List<String> urls = new ArrayList<>();
        urls.add("https://www.motogama.pl/pdf_files/katalogipdf/1.pdf");
        urls.add("http://99.99.99.99/999");
        mvc.perform(post("/api/iiif/{transcriptionId}/download-images", transcriptionId)
                .content(objectMapper.writeValueAsString(urls)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

}
