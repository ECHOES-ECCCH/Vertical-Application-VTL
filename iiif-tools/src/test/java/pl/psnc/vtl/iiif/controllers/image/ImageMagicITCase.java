package pl.psnc.vtl.iiif.controllers.image;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import pl.psnc.vtl.iiif.services.image.ProcessBuilderService;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class ImageMagicITCase {

    @Autowired
    private MockMvc mvc;

    @Mock
    ProcessBuilderService processBuilderService;

    @Mock
    Process process;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void shouldReturnPagesForFilesList()
            throws Exception {
        Integer transcriptionId = 1;
        List<String> filesList = Arrays.asList("multipage_test.tif", "document.pdf", "image2.jpeg", "page0249.tiff");

        mvc.perform(post("/api/iiif/{transcriptionId}/pages", transcriptionId)
                .content(objectMapper.writeValueAsString(filesList))
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }


}
