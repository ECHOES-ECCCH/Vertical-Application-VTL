package pl.psnc.vtl.iiif.controllers.image;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class PdfbeadsControllerITCase {

    @Autowired
    private MockMvc mvc;


    @Ignore("TEST nie działa i używa plików testowych z /src/main/resources")
    @Test
    public void shouldReturnPdf()
            throws Exception {
        long transcriptionId = 1;
        String hash = "someTestHash";

        byte[] hocr1Content = java.nio.file.Files
                .readAllBytes(Path.of("src/main/resources/t1/hocrWithComplexStructure.html"));
        byte[] tif1Content = java.nio.file.Files
                .readAllBytes(Path.of("src/main/resources/t1/hocrWithComplexStructure.tif"));
        byte[] hocr2Content = java.nio.file.Files
                .readAllBytes(Path.of("src/main/resources/t1/hocrWithParagraphsOnly.html"));
        byte[] tif2Content = java.nio.file.Files
                .readAllBytes(Path.of("src/main/resources/t1/hocrWithParagraphsOnly.tif"));

        String filename1 = "hocrWithComplexStructure0001.html";
        String filename2 = "hocrWithComplexStructure0002.html";
        String filename3 = "hocrWithParagraphsOnly0003.html";
        String filename4 = "hocrWithParagraphsOnly0004.html";

        MockMultipartFile hocr1 = new MockMultipartFile("files", filename1, "text/plain", hocr1Content);
        MockMultipartFile tif1 = new MockMultipartFile("files", filename2, "text/plain", tif1Content);
        MockMultipartFile hocr2 = new MockMultipartFile("files", filename3, "text/plain", hocr2Content);
        MockMultipartFile tif2 = new MockMultipartFile("files", filename4, "text/plain", tif2Content);

        String exampleUrl = "1/page250_Page0000.tiff/info.json";
        Map<String, String> mapping = new HashMap<>();
        mapping.put(filename1, exampleUrl);
        mapping.put(filename2, exampleUrl);
        mapping.put(filename3, exampleUrl);
        mapping.put(filename4, exampleUrl);
        JSONObject iiifUrlsMap = new JSONObject(mapping);
        String mappingString = iiifUrlsMap.toJSONString();

        mvc.perform(MockMvcRequestBuilders.multipart("/api//transcriptions/{Id}/eBooks", transcriptionId).file(hocr1)
                .file(tif1).file(hocr2).file(tif2).param("format", "PDF").param("hash", hash)
                .param("title", "mobiTitle").param("author", "author").param("mapping", mappingString))
                .andExpect(status().isCreated()).andReturn();

    }

}