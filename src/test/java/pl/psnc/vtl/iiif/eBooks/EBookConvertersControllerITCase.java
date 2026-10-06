package pl.psnc.vtl.iiif.eBooks;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class EBookConvertersControllerITCase {

    @Autowired
    private MockMvc mvc;

    private MultipartFileGenerator multipartFileGenerator = new MultipartFileGenerator();


    @Test
    @Ignore("No system file access")
    public void shouldCreateMobiAndReturnItsLocation()
            throws Exception {
        byte[] ba = multipartFileGenerator
                .generateMultipartFile("src/main/resources/epubTestHtml/1fileNo1.html", "1fileNo1.html").getBytes();

        String filename = "1fileNo1.html";
        MockMultipartFile file1 = new MockMultipartFile("files", filename, "text/plain", ba);

        int transcriptionId = 1;
        String hash = "someNewTestHash";

        String exampleUrl = "1/page250_Page0000.tiff/info.json";
        Map<String, String> mapping = new HashMap<>();
        mapping.put(filename, exampleUrl);
        JSONObject iiifUrlsMap = new JSONObject(mapping);
        String mappingString = iiifUrlsMap.toJSONString();

        MvcResult result = mvc.perform(
                MockMvcRequestBuilders.multipart("/api//transcriptions/{Id}/eBooks", transcriptionId).file(file1)
                        .param("format", "MOBI").param("hash", hash).param("title", "mobiTitle")
                        .param("author", "author").param("mapping", mappingString)).andExpect(status().isCreated()).andReturn();

        String location = result.getResponse().getHeader("Location");

        assertTrue(location.contains("/ebooks/t1/mobi/" + hash + ".mobi"));
    }

}